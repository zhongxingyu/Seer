 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.redshape.persistence.dao.jpa.executors;
 
 import com.redshape.persistence.dao.query.IQuery;
 import com.redshape.persistence.dao.query.OrderDirection;
 import com.redshape.persistence.dao.query.QueryExecutorException;
 import com.redshape.persistence.dao.query.executors.AbstractQueryExecutor;
 import com.redshape.persistence.dao.query.executors.IDynamicQueryExecutor;
 import com.redshape.persistence.dao.query.expressions.*;
 import com.redshape.persistence.dao.query.expressions.operations.BinaryOperation;
 import com.redshape.persistence.dao.query.expressions.operations.UnaryOperation;
 import com.redshape.persistence.dao.query.statements.*;
 import com.redshape.persistence.entities.IEntity;
 import com.redshape.utils.Commons;
 import com.redshape.utils.StringUtils;
 import org.hibernate.ejb.criteria.path.PluralAttributePath;
 import org.hibernate.shards.util.StringUtil;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.persistence.criteria.*;
 import javax.persistence.metamodel.PluralAttribute;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  * @author user
  */
 public class CriteriaExecutor extends AbstractQueryExecutor<Query, Predicate, Expression<?>>
                               implements IDynamicQueryExecutor<Query, CompoundSelection<?>> {
     private EntityManager manager;
     private CriteriaBuilder builder;
     private CriteriaQuery<IEntity> criteria;
     private Root<?> root;
 
     public CriteriaExecutor( EntityManager manager, IQuery query) {
         super(query);
 
         this.manager = manager;
         this.builder = manager.getCriteriaBuilder();
         this.criteria = this.getBuilder().createQuery( this.getQuery().getEntityClass() );
         this.root = this.criteria.from( query.<IEntity>getEntityClass() );
         this.builder = manager.getCriteriaBuilder();
     }
 
     protected EntityManager getManager() {
         return this.manager;
     }
 
     protected CriteriaBuilder getBuilder() {
         return this.builder;
     }
 
     protected CriteriaQuery<IEntity> getCriteria() {
         return this.criteria;
     }
 
     @Override
     protected Query processResult( Predicate expression  ) throws QueryExecutorException {
         if ( expression != null ) {
     	    this.getCriteria().where( expression );
         }
         
         List<IStatement> paths = this.getQuery().select();
         if ( !paths.isEmpty() ) {
             for ( int i = 0; i < paths.size(); i++ ) {
                 this.getCriteria().select( (Selection<? extends IEntity>) this.processStatement( paths.get(i) ) );
             }
         }
 
         if ( this.getQuery().orderField() != null ) {
             Expression<?> fieldStm = this.processStatement( this.getQuery().orderField() );
             OrderDirection direction = Commons.select( this.getQuery().orderDirection(), OrderDirection.DESC );
             switch( direction ) {
                 case ASC:
                     this.getCriteria().orderBy( this.getBuilder().asc( fieldStm ) );
                 break;
                 case DESC:
                     this.getCriteria().orderBy( this.getBuilder().desc( fieldStm ) );
                 break;
                 default:
                     throw new QueryExecutorException("Unknown order direction");
             }
         }
             
         List<IStatement> groupBy = this.getQuery().groupBy();
         if ( !groupBy.isEmpty() ) {
             Expression<?>[] groupByFields = new Expression<?>[ this.getQuery().groupBy().size() ];
             for ( int i = 0; i < groupBy.size(); i++ ) {
                 groupByFields[i] = this.processStatement(groupBy.get(i));
             }
             this.getCriteria().groupBy( groupByFields );
         }
         
         Query nativeQuery = this.getManager().createQuery( this.getCriteria() );
         for ( String key : this.getQuery().getAttributes().keySet() ) {
         	if ( !this.getQuery().hasAttribute( key ) ) {
         		throw new QueryExecutorException();
         	}
         	
         	nativeQuery.setParameter( key, this.getQuery().getAttribute( key ) );
         }
 
         return nativeQuery;
     }
 
     @Override
     public Expression<?> processExpression(InExpression expression) throws QueryExecutorException {
         return this.processStatement( expression.getField() ).in( this.processStatement(expression.getRange()) );
     }
 
     @Override
     public Expression<?> processExpression(LikeExpression expression) throws QueryExecutorException {
         return this.getBuilder().like(
                 (Expression<String>) this.processStatement(expression.getField()),
                 (Expression<String>) this.processStatement(expression.getMask())
         );
     }
 
     @Override
     public Expression<?> processExpression(UnaryOperation operation) throws QueryExecutorException {
         switch ( operation.getType() ) {
             case NEGATE:
                 return this.getBuilder().neg(
                         (Expression<Number>) this.processStatement( operation.getTerm() ) );
             default:
                 throw new QueryExecutorException("Unsupported unary operation "
                         + Commons.select( operation.getType(), "<null>" ) );
         }
     }
 
     @Override
     public Expression<?> processExpression(BinaryOperation operation) throws QueryExecutorException {
         switch ( operation.getType() ) {
             case SUM:
                 return this.getBuilder().sum(
                     (Expression<Number>) this.processStatement( operation.getLeft() ),
                     (Expression<Number>) this.processStatement( operation.getRight() )
                 );
             case SUBTRACT:
                 return this.getBuilder().diff(
                     (Expression<Number>) this.processStatement( operation.getLeft() ),
                     (Expression<Number>) this.processStatement( operation.getRight() )
                 );
             case DIVIDE:
                 return this.getBuilder()
                 .quot(
                     (Expression<Number>) this.processStatement( operation.getLeft() ),
                     (Expression<Number>) this.processStatement( operation.getRight() )
                 );
             case MOD:
                 return this.getBuilder().mod(
                     (Expression<Integer>) this.processStatement( operation.getLeft() ),
                     (Expression<Integer>) this.processStatement( operation.getRight() )
                 );
             case PROD:
                 return this.getBuilder().prod(
                     (Expression<Number>) this.processStatement( operation.getLeft() ),
                     (Expression<Number>) this.processStatement( operation.getRight() )
                 );
             default:
                 throw new QueryExecutorException("Unsupported binary operation "
                         + Commons.select( operation.getType(), "<null>") );
         }
     }
 
     @Override
     public Expression<?> processExpression(FunctionExpression expression) throws QueryExecutorException {
         Expression[] expressions = new Expression[expression.getTerms().length];
         int i = 0;
         for ( IStatement term : expression.getTerms() ) {
             expressions[i++] = this.processStatement(term);
         }
 
         Expression<?> result =  this.getBuilder().function(
             expression.getName(),
             Object.class,
             expressions
         );
 
         return result;
     }
 
     @Override
     public CompoundSelection<?> processStatement(ArrayStatement statement) throws QueryExecutorException {
         Expression<?>[] statements = new Expression<?>[statement.getSize()];
         for ( int i = 0; i < statements.length; i++ ) {
             statements[i++] = this.processStatement( statement.getStatement(i) );
         }
 
         return this.getBuilder().array( statements );
     }
 
     @Override
     public Expression<?> processStatement(JoinStatement statement ) throws QueryExecutorException {
         String[] path = statement.getName().split("\\.");
         Join<?, ?> joinContext = null;
         int i = 0;
         for ( String pathPart : path ) {
             if ( joinContext == null ) {
                 joinContext = this.root.join( pathPart );
             } else {
                 joinContext = joinContext.join( pathPart );
             }
         }
 
         return joinContext;
     }
 
     @Override
     public Expression<?> processStatement(ScalarStatement<?> scalar) throws QueryExecutorException {
         Object value = scalar.getValue();
         if ( value instanceof IEntity ) {
             throw new QueryExecutorException("Not supported. Use dot-based (user.id) path notation");
         }
 
         if ( value != null ) {
             return this.getBuilder().literal( value );
         } else {
             return this.getBuilder().nullLiteral( Object.class );
         }
     }
 
     @Override
     public Expression<?> processStatement(ReferenceStatement reference) throws QueryExecutorException {
         return this.resolvePath(reference.getValue());
     }
     
     protected Expression<?> resolvePath( String path ) throws QueryExecutorException {
         if ( path == null ) {
             return this.getBuilder().nullLiteral(Object.class);
         }
 
         String[] parts = path.toString().split("\\.");
         int offset = 0;
         Path<?> pathContext = this.root;
         Path<?> prevPathContext = this.root;
         while ( offset < parts.length ) {
             pathContext = pathContext.get(parts[offset++]);
             if ( pathContext instanceof PluralAttributePath ) {
                 From<?, ?> joinContext = prevPathContext instanceof From ? (From<?, ?>) prevPathContext : this.root;
                 for ( Join<?, ?> join : joinContext.getJoins() ) {
                     if ( ((PluralAttributePath) pathContext).getAttribute()
                             .getName().equals( join.getAttribute().getName() ) ) {
                         pathContext = join;
                     }
                 }
             }
 
             prevPathContext = pathContext;
         }
 
         return pathContext;
     }
     
     @Override
     public Predicate processExpression(EqualsOperation expression) throws QueryExecutorException {
         return this.getBuilder().equal(
             this.processStatement( expression.getLeftOperand() ),
             this.processStatement( expression.getRightOperand() )
         );
     }
 
     @Override
     public Predicate processExpression(LessThanOperation less) throws QueryExecutorException {
         return this.getBuilder().lt(
             this.<Expression<Number>>processStatement( less.getLeftOperand() ),
             this.<Expression<Number>>processStatement( less.getRightOperand() )
         );
     }
 
     @Override
     public Predicate processExpression(GreaterThanOperation greater) throws QueryExecutorException {
         return this.getBuilder().gt(
             this.<Expression<Number>>processStatement( greater.getLeftOperand() ),
             this.<Expression<Number>>processStatement( greater.getRightOperand() )
         );
     }
 
     @Override
     public Predicate processExpression(AndExpression and) throws QueryExecutorException {
         Predicate[] expressions = new Predicate[ and.getTerms().length ];
 
         int i = 0;
         for ( IExpression part : and.getTerms() ) {
             expressions[i++] = this.processExpression( part );
         }
 
         return this.getBuilder().and(expressions);
     }
 
     @Override
     public Predicate processExpression(OrExpression or) throws QueryExecutorException {
     	Predicate[] expressions = new Predicate[ or.getTerms().length ];
 
         int i = 0;
         for ( IExpression part : or.getTerms() ) {
             expressions[i++] = this.processExpression( part );
         }
 
         return this.getBuilder().or( expressions );
     }
 
     @Override
     public Predicate processExpression(NotOperation not) throws QueryExecutorException {
         return this.getBuilder().not( this.processExpression( not.getExpression() ) );
     }
 
 
 }

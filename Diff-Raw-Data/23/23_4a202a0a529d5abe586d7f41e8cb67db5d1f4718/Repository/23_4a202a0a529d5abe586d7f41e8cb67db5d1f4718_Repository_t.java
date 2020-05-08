 package com.marshmallowswisdom.liber.persistence;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.Persistence;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Join;
 import javax.persistence.criteria.JoinType;
 import javax.persistence.criteria.Root;
 
 import com.marshmallowswisdom.liber.domain.Article;
 import com.marshmallowswisdom.liber.domain.ArticleVersion;
 import com.marshmallowswisdom.liber.domain.Field;
 import com.marshmallowswisdom.liber.domain.FieldValue;
 import com.marshmallowswisdom.liber.domain.HierarchicalFieldValue;
 import com.marshmallowswisdom.liber.domain.Tag;
 
 public class Repository {
 	
 	private final EntityManagerFactory factory;
 	
 	public Repository() {
 		factory = Persistence.createEntityManagerFactory( "liber" );
 	}
 	
 	public ArticleVersion saveArticleVersion( ArticleVersion articleVersion ) {
 		final EntityManager entityManager = factory.createEntityManager();
 		final EntityTransaction transaction = entityManager.getTransaction();
 		transaction.begin();
 		articleVersion = entityManager.merge( articleVersion );
 		transaction.commit();
 		entityManager.close();
 		return articleVersion;
 	}
 	
 	public Article saveNewArticle( Article article, ArticleVersion firstVersion ) {
 		final EntityManager entityManager = factory.createEntityManager();
 		final EntityTransaction transaction = entityManager.getTransaction();
 		transaction.begin();
 		firstVersion = entityManager.merge( firstVersion );
 		article = firstVersion.getArticle();
 		article.setLatestVersion( firstVersion );
 		transaction.commit();
 		entityManager.close();
 		return article;
 	}
 	
 	public Article saveArticle( Article article ) {
 		final EntityManager entityManager = factory.createEntityManager();
 		final EntityTransaction transaction = entityManager.getTransaction();
 		transaction.begin();
 		article = entityManager.merge( article );
 		transaction.commit();
 		entityManager.close();
 		return article;
 	}
 
 	public List<Tag> retrieveTags() {
 		final EntityManager entityManager = factory.createEntityManager();
 		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 		final CriteriaQuery<Tag> query = criteriaBuilder.createQuery( Tag.class );
 		final Root<Tag> root = query.from( Tag.class );
 		query.select( root );
 		final List<Tag> tags = entityManager.createQuery( query ).getResultList();
 		entityManager.close();
 		return tags;
 	}
 
 	public Tag retrieveTag( final int id ) {
 		final EntityManager entityManager = factory.createEntityManager();
 		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 		final CriteriaQuery<Tag> query = criteriaBuilder.createQuery( Tag.class );
 		final Root<Tag> root = query.from( Tag.class );
 		root.fetch( "childTags", JoinType.LEFT );
 		root.fetch( "articles", JoinType.LEFT );
 		query.where( criteriaBuilder.equal( root.get( "id" ), id ) );
 		final Tag tag = entityManager.createQuery( query ).setMaxResults( 1 ).getSingleResult();
 		Tag parent = tag.getParent();
 		while( parent != null ) {
 			parent = parent.getParent();
 		}
 		entityManager.close();
 		return tag;
 	}
 	
 	public Tag retrieveTag( final String name ) {
 		final EntityManager entityManager = factory.createEntityManager();
 		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 		final CriteriaQuery<Tag> query = criteriaBuilder.createQuery( Tag.class );
 		final Root<Tag> root = query.from( Tag.class );
 		query.where( criteriaBuilder.equal( root.get( "name" ), name ) );
 		final Tag tag = entityManager.createQuery( query ).setMaxResults( 1 ).getSingleResult();
 		entityManager.close();
 		return tag;
 	}
 	
 	public Tag retrieveTagByPath( final String path ) {
 		final EntityManager entityManager = factory.createEntityManager();
 		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 		final CriteriaQuery<Tag> query = criteriaBuilder.createQuery( Tag.class );
 		final Root<Tag> root = query.from( Tag.class );
 		root.fetch( "articles", JoinType.LEFT );
 		query.where( criteriaBuilder.equal( root.get( "path" ), path ) );
 		final Tag tag = entityManager.createQuery( query ).setMaxResults( 1 ).getSingleResult();
 		entityManager.close();
 		return tag;
 	}
 
 	public Tag saveTag( Tag tag ) {
 		final EntityManager entityManager = factory.createEntityManager();
 		final EntityTransaction transaction = entityManager.getTransaction();
 		transaction.begin();
 		tag = entityManager.merge( tag );
 		transaction.commit();
 		entityManager.close();
 		return tag;
 	}
 
 	public Article retrieveArticle( final int id ) {
 		final EntityManager entityManager = factory.createEntityManager();
 		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 		final CriteriaQuery<Article> query = criteriaBuilder.createQuery( Article.class );
 		final Root<Article> root = query.from( Article.class );
 		query.where( criteriaBuilder.equal( root.get( "id" ), id ) );
 		final Article article = entityManager.createQuery( query ).getSingleResult();
 		entityManager.close();
 		return article;
 	}
 
 	public List<Article> retrieveArticlesByTag( final int tagId ) {
 		final EntityManager entityManager = factory.createEntityManager();
 		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 		final CriteriaQuery<Article> query = criteriaBuilder.createQuery( Article.class );
 		final Root<Article> root = query.from( Article.class );
 		Join<Article,ArticleVersion> version = root.join( "versions", JoinType.LEFT );
 		Join<ArticleVersion,Tag> tag = version.join( "tags", JoinType.LEFT );
 		query.where( criteriaBuilder.equal( tag.get( "id" ), tagId ) );
 		final List<Article> articles = entityManager.createQuery( query ).getResultList();
 		entityManager.close();
 		return articles;
 	}
 
 	public void deleteArticle( final int id ) {
 		final EntityManager entityManager = factory.createEntityManager();
 		final Article article = entityManager.find( Article.class, id );
 		final EntityTransaction transaction = entityManager.getTransaction();
 		transaction.begin();
 		for( ArticleVersion version : article.getVersions() ) {
 			for( Tag tag : version.getTags() ) {
 				tag.removeArticle( version );
 			}
 			version.removeTags();
 		}
 		entityManager.remove( article );
 		transaction.commit();
 		entityManager.close();
 	}
 
 	public List<Field> retrieveFields() {
 		final EntityManager entityManager = factory.createEntityManager();
 		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 		final CriteriaQuery<Field> query = criteriaBuilder.createQuery( Field.class );
 		final Root<Field> root = query.from( Field.class );
 		query.select( root );
 		final List<Field> fields = entityManager.createQuery( query ).getResultList();
 		entityManager.close();
 		return fields;
 	}
 
 	public Field retrieveField( final int id ) {
 		final EntityManager entityManager = factory.createEntityManager();
 		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 		final CriteriaQuery<Field> query = criteriaBuilder.createQuery( Field.class );
 		final Root<Field> root = query.from( Field.class );
 		query.where( criteriaBuilder.equal( root.get( "id" ), id ) );
 		final Field field = entityManager.createQuery( query ).getSingleResult();
 		entityManager.close();
 		return field;
 	}
 
 	public Field saveField( final Field field ) {
 		final EntityManager entityManager = factory.createEntityManager();
 		final EntityTransaction transaction = entityManager.getTransaction();
 		transaction.begin();
 		final Field savedField = entityManager.merge( field );
 		transaction.commit();
 		entityManager.close();
 		return savedField;
 	}
	
	public void deleteField( final int id ) {
		final EntityManager entityManager = factory.createEntityManager();
		final Field field = entityManager.find( Field.class, id );
		final EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		entityManager.remove( field );
		transaction.commit();
		entityManager.close();
	}
 
 	public HierarchicalFieldValue retrieveFieldValue( final int id )  {
 		final EntityManager entityManager = factory.createEntityManager();
 		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 		final CriteriaQuery<HierarchicalFieldValue> query = 
 				criteriaBuilder.createQuery( HierarchicalFieldValue.class );
 		final Root<HierarchicalFieldValue> root = query.from( HierarchicalFieldValue.class );
 		root.fetch( "childValues", JoinType.LEFT );
 		query.where( criteriaBuilder.equal( root.get( "id" ), id ) );
 		final HierarchicalFieldValue value = entityManager.createQuery( query ).getSingleResult();
 		entityManager.close();
 		return value;
 	}
 
 	public FieldValue saveFieldValue( final FieldValue value ) {
 		final EntityManager entityManager = factory.createEntityManager();
 		final EntityTransaction transaction = entityManager.getTransaction();
 		transaction.begin();
 		final FieldValue savedValue = entityManager.merge( value );
 		transaction.commit();
 		entityManager.close();
 		return savedValue;
 	}
 
 }

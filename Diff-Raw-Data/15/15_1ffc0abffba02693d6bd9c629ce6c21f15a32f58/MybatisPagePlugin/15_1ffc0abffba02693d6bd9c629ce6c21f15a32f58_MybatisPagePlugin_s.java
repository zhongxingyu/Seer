 
 //------------------------------------------------------------------------------
 //文   件  名： com.hp.util.base.PagePlugin     版           本：1.0
 //描          述：
 //版权所有：杭州瀚鹏科技有限公司 
 //------------------------------------------------------------------------------
 //创  建   者：Invalid          创建日期：2012-4-12
 //修  改   者：                                             修改日期：
 //修改说明：
 //------------------------------------------------------------------------------
 package com.impler.ipage.plugin;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.ibatis.builder.xml.dynamic.ForEachSqlNode;
 import org.apache.ibatis.executor.ErrorContext;
 import org.apache.ibatis.executor.ExecutorException;
 import org.apache.ibatis.executor.statement.BaseStatementHandler;
 import org.apache.ibatis.executor.statement.RoutingStatementHandler;
 import org.apache.ibatis.executor.statement.StatementHandler;
 import org.apache.ibatis.mapping.BoundSql;
 import org.apache.ibatis.mapping.MappedStatement;
 import org.apache.ibatis.mapping.ParameterMapping;
 import org.apache.ibatis.mapping.ParameterMode;
 import org.apache.ibatis.mapping.SqlCommandType;
 import org.apache.ibatis.plugin.Interceptor;
 import org.apache.ibatis.plugin.Intercepts;
 import org.apache.ibatis.plugin.Invocation;
 import org.apache.ibatis.plugin.Plugin;
 import org.apache.ibatis.plugin.Signature;
 import org.apache.ibatis.reflection.MetaObject;
 import org.apache.ibatis.reflection.property.PropertyTokenizer;
 import org.apache.ibatis.session.Configuration;
 import org.apache.ibatis.type.TypeHandler;
 import org.apache.ibatis.type.TypeHandlerRegistry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.impler.ipage.DefaultPageImpl;
 import com.impler.ipage.Page;
 import com.impler.ipage.PageDialect;
 import com.impler.ipage.PageUtil;
 import com.impler.ipage.ReflectHelper;
 
 /**
  * Mybatis 分页插件
  * @author  Invalid
  * @date 2012-4-12 下午3:02:22
  */
 @Intercepts({@Signature(type=StatementHandler.class,method="prepare",args={Connection.class})})
 public class MybatisPagePlugin implements Interceptor {
 	
 	private static final Logger log = LoggerFactory.getLogger(MybatisPagePlugin.class);
 	
 	private PageDialect dialect; //数据库方言  
     private String pageSqlId = ""; //mapper.xml中需要拦截的ID(正则匹配) 
 
 	/* (non-Javadoc)
 	 * @see org.apache.ibatis.plugin.Interceptor#intercept(org.apache.ibatis.plugin.Invocation)
 	 * @author  Invalid
 	 * @date 2012-4-12 下午3:05:00
 	 */
 	@Override
 	public Object intercept(Invocation invocation) throws Throwable {
         if(invocation.getTarget() instanceof RoutingStatementHandler){ 
         	
             RoutingStatementHandler statementHandler = (RoutingStatementHandler)invocation.getTarget();  
             BaseStatementHandler delegate = (BaseStatementHandler) ReflectHelper.getValueByFieldName(statementHandler, "delegate");  
             MappedStatement mappedStatement = (MappedStatement) ReflectHelper.getValueByFieldName(delegate, "mappedStatement");  
             
             if(mappedStatement.getSqlCommandType()==SqlCommandType.SELECT
             		&& mappedStatement.getId().matches(pageSqlId)){ //拦截需要分页的SQL  
             	
                 BoundSql boundSql = delegate.getBoundSql(); 
                 String sql = boundSql.getSql();  
                 Object parameterObject = boundSql.getParameterObject();//分页SQL<select>中parameterType属性对应的实体参数，即Mapper接口中执行分页方法的参数,该参数不得为空  
                 Page page = null;  
                 if(parameterObject==null){  
                 	page = new DefaultPageImpl();
                 	log.error("分页查询的parameterObject尚未实例化！");  
                 }else{  
                 	boolean setValueByFieldName = false;
                 	String fieldname = "";
                 	if(parameterObject instanceof Page){    //参数就是Page实体  
                         page = (Page) parameterObject;  
                         page.setEntityOrField(true);    //见com.flf.entity.Page.entityOrField 注释  
                          
 	                }else{  //参数为某个实体，该实体拥有Page属性  
 	                	fieldname = PageUtil.getFieldNameImplPage(parameterObject);
 	                   		
 	                   	if(fieldname!=null)
 	                   		page = (Page) ReflectHelper.getValueByFieldName(parameterObject,fieldname);  
 	                   	
 	                    if(page==null)
 	                       	page = new DefaultPageImpl();
 	                    
 	                   	page.setEntityOrField(false); 
 	                   	setValueByFieldName = true;
 	                }
                 	
                 	
                     Connection connection = (Connection) invocation.getArgs()[0];  
                     //String countSql = "select count(0) from (" + sql+ ") as tmp_count"; //记录统计  
                     sql = sql.toLowerCase();
                     String countSql =getCountSql(sql); //记录统计
                     
                     PreparedStatement countStmt = connection.prepareStatement(countSql);  
                     BoundSql countBS = new BoundSql(mappedStatement.getConfiguration(),countSql,boundSql.getParameterMappings(),parameterObject);  
                     setParameters(countStmt,mappedStatement,countBS,parameterObject);  
                     ResultSet rs = countStmt.executeQuery();  
                     int count = 0;  
                     if (rs.next()) {  
                         count = rs.getInt(1);  
                     }  
                     rs.close();  
                     countStmt.close();  
                     
                     page.setTotalResult(count); 
                     if(setValueByFieldName)
                     	ReflectHelper.setValueByFieldName(parameterObject,fieldname, page); //通过反射，对实体对象设置分页对象
                 }
                 
                 ReflectHelper.setValueByFieldName(boundSql, 
                 								  "sql", 
                 								  dialect.generatePageSql(sql,page)); //将分页sql语句反射回BoundSql.  
             }
         }  
         return invocation.proceed();  
 	}
 
 	/* (non-Javadoc)
 	 * @see org.apache.ibatis.plugin.Interceptor#plugin(java.lang.Object)
 	 * @author  Invalid
 	 * @date 2012-4-12 下午3:05:00
 	 */
 	@Override
 	public Object plugin(Object target) {
 		return Plugin.wrap(target, this);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.apache.ibatis.plugin.Interceptor#setProperties(java.util.Properties)
 	 * @author  Invalid
 	 * @date 2012-4-12 下午3:05:00
 	 */
 	@Override
 	public void setProperties(Properties properties) {
 		setDialect(properties.getProperty("dialect"));  
         if (dialect==null)
         	log.error("dialect property is not found!");
         pageSqlId = properties.getProperty("pageSqlId");  
         if (pageSqlId==null||pageSqlId.equals(""))
         	log.error("pageSqlId property is not found!");  
 	}
 	
 	/** 
      * 对SQL参数(?)设值,参考org.apache.ibatis.executor.parameter.DefaultParameterHandler 
      * @param ps 
      * @param mappedStatement 
      * @param boundSql 
      * @param parameterObject 
      * @throws SQLException 
      */  
     private void setParameters(PreparedStatement ps,MappedStatement mappedStatement,BoundSql boundSql,Object parameterObject) throws SQLException {  
         ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());  
         List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();  
         if (parameterMappings != null) {  
             Configuration configuration = mappedStatement.getConfiguration();  
             TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();  
             MetaObject metaObject = parameterObject == null ? null: configuration.newMetaObject(parameterObject);  
             for (int i = 0; i < parameterMappings.size(); i++) {  
                 ParameterMapping parameterMapping = parameterMappings.get(i);  
                 if (parameterMapping.getMode() != ParameterMode.OUT) {  
                     Object value;  
                     String propertyName = parameterMapping.getProperty();  
                     PropertyTokenizer prop = new PropertyTokenizer(propertyName);  
                     if (parameterObject == null) {  
                         value = null;  
                     } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {  
                         value = parameterObject;  
                     } else if (boundSql.hasAdditionalParameter(propertyName)) {  
                         value = boundSql.getAdditionalParameter(propertyName);  
                     } else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX)&& boundSql.hasAdditionalParameter(prop.getName())) {  
                         value = boundSql.getAdditionalParameter(prop.getName());  
                         if (value != null) {  
                             value = configuration.newMetaObject(value).getValue(propertyName.substring(prop.getName().length()));  
                         }  
                     } else {  
                         value = metaObject == null ? null : metaObject.getValue(propertyName);  
                     }  
                     
 					@SuppressWarnings("unchecked")
 					TypeHandler<Object> typeHandler = parameterMapping.getTypeHandler();  
                     if (typeHandler == null) {  
                         throw new ExecutorException("There was no TypeHandler found for parameter "+ propertyName + " of statement "+ mappedStatement.getId());  
                     }  
                     typeHandler.setParameter(ps, i + 1, value, parameterMapping.getJdbcType());  
                 }  
             }  
         }  
     }
 
     private static String getCountSql(String sql){
     	sql=sql.trim();
    	if(sql.startsWith("(")&&sql.endsWith(""))
     		sql = sql.substring(1,sql.length()-1);
     	int len =  getFromIndex(sql);
         String countSql = "select count(*) " + sql.substring(len); //记录统计
         int oindex= countSql.lastIndexOf("order");
        int left = sql.indexOf("(",oindex);
        int right = sql.indexOf(")",oindex);
        
        if(right<0||(left>0&&left<right)){
        	countSql = countSql.substring(0, oindex);
         }
     	return countSql;
     }
     
     private static int getFromIndex(String sql){
         int len= sql.indexOf("from");
         int left = sql.indexOf("(",len);
         int right = sql.indexOf(")",len);
         while(right>0&&(left<0||left>right)){
         	len= sql.indexOf("from",len+1);
             left = sql.indexOf("(",len);
             right = sql.indexOf(")",len);
         }
         
         char b = sql.charAt(len-1);
         if((b>='0'&&b<='9')||(b>='a'&&b<='z')||b=='_')
             return len+4+getFromIndex(sql.substring(len+4));
         b = sql.charAt(len+4);
         if((b>='0'&&b<='9')||(b>='a'&&b<='z')||b=='_')
             return len+4+getFromIndex(sql.substring(len+4));
         return len;
     }
 	
 	public void setDialect(String dialect) {
 		this.dialect = PageDialect.valueOf(dialect);
 	}
 
 	public void setPageSqlId(String pageSqlId) {
 		this.pageSqlId = pageSqlId;
 	}  
 	
 }
 

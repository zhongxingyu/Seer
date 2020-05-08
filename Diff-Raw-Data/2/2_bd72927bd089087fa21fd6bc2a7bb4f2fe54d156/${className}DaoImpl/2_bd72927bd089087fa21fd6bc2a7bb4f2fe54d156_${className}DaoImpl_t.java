 <#assign className = table.className>   
 <#assign classNameLower = className?uncap_first>   
 package ${basepackage}.dao.impl;
 
 import java.io.Serializable;
 import java.util.List;
 
 import org.springframework.stereotype.Repository;
 
 import ${basepackage}.dao.${className}Dao;
 import ${basepackage}.po.${className};
 import ${import_common}.BaseSqlMapDao;
 import ${import_common}.Page;
 import ${import_common}.QueryRequest;
 
 @Repository("${classNameLower}Dao")
 public class ${className}DaoImpl extends BaseSqlMapDao<${className}> implements ${className}Dao{
 	
     public int save(${className} ${classNameLower}) {
         return getSqlSessionTemplate().insert("${classNameLower}.insert", ${classNameLower});
     }
 
     public int update(${className} ${classNameLower}) {
         return getSqlSessionTemplate().update("${classNameLower}.update", ${classNameLower});
     }
     
     public int saveOrUpdate(${className} ${classNameLower}) {
         if(${classNameLower}.getId() == null) 
             return save(${classNameLower});
         else
             return update(${classNameLower});
     }
     
     public int deleteById(Serializable id) {
         return getSqlSessionTemplate().delete("${classNameLower}.deleteById", id);
     }
     
     public int deleteByIds(List<Serializable> ids) {
         if (ids == null || ids.isEmpty()) {
             return 0;
         }
         return getSqlSessionTemplate().delete("${classNameLower}.deleteByIds", ids);
     }
 
     public ${className} getById(Serializable id) {
         ${className} object = (${className}) getSqlSessionTemplate().selectOne("${classNameLower}.getById", id);
         return object;
     }
     
     public List<${className}> findByIds(List<Serializable> ids) {
         List<${className}> ${classNameLower}List = getSqlSessionTemplate().selectList("${classNameLower}.findByIds", ids);
         return ${classNameLower}List;
     }
     
     public List<${className}> find(${className} ${classNameLower}){
         List<${className}> ${classNameLower}List = getSqlSessionTemplate().selectList("${classNameLower}.find", ${classNameLower});
         return ${classNameLower}List;
     }
     
     public Page<${className}> queryPage(Integer pageIndex, Integer sizePerPage, ${className} ${classNameLower}) {
        QueryRequest<${className}> queryRequest = new QueryRequest<${className}>(pageIndex, sizePerPage, ${classNameLower});
         return this.findPage(queryRequest);
     }
     
     <#list table.columns as column>
     <#if column.unique && !column.pk>
     public ${className} getBy${column.columnName}(${column.javaType} ${column.columnNameFirstLower}) {
         return (${className})getSqlSessionTemplate().selectOne("${classNameLower}.getBy${column.columnName}",${column.columnNameFirstLower});
     }
     
     </#if>
     </#list>
     
     public String getSqlMapNamesapce() {
         return "${classNameLower}";
     }
 
 }

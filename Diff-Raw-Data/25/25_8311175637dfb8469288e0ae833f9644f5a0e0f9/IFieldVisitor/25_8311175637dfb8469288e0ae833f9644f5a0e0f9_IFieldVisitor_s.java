 package com.redshape.search.index.visitor.field;
 
 import com.redshape.search.index.IIndex;
 import com.redshape.search.index.visitor.VisitorException;
 
import java.lang.reflect.Field;

 /**
  * Created by IntelliJ IDEA.
  * User: nikelin
  * Date: Aug 13, 2010
  * Time: 2:51:05 PM
  * To change this template use File | Settings | File Templates.
  */
 public interface IFieldVisitor {
 
    public void visitField( IIndex index, Class<?> entityClass, Field field )
 			throws VisitorException;
 
 }

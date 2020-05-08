 /**
  * Licensed to Jasig under one or more contributor license
  * agreements. See the NOTICE file distributed with this work
  * for additional information regarding copyright ownership.
  * Jasig licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a
  * copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.jasig.portal.security.provider.saml;
 
 import java.util.concurrent.TimeUnit;
 
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.namespace.QName;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
 import org.apache.commons.pool.impl.GenericKeyedObjectPool;
 
 /**
  * @author Eric Dalquist
  * @version $Revision$
  */
 public class XPathExpressionPool {
     private final GenericKeyedObjectPool pool;
     private final NamespaceContext namespaceContext;
     
     public XPathExpressionPool() {
         this(null);
     }
     
     public XPathExpressionPool(NamespaceContext namespaceContext) {
         this.namespaceContext = namespaceContext;
         
         final XPathExpressionFactory xpathExpressionfactory = new XPathExpressionFactory();
         this.pool = new GenericKeyedObjectPool(xpathExpressionfactory);
         this.pool.setMaxActive(100);
         this.pool.setMaxIdle(100);
         this.pool.setTimeBetweenEvictionRunsMillis(TimeUnit.SECONDS.toMillis(60));
         this.pool.setMinEvictableIdleTimeMillis(TimeUnit.MINUTES.toMillis(5));
         this.pool.setNumTestsPerEvictionRun(this.pool.getMaxIdle() / 6);
     }
     
     @Override
     protected void finalize() throws Throwable {
         this.pool.close();
     }
 
     public <T> T doWithExpression(String expression, XPathExpressionCallback<T> callback) throws XPathExpressionException {
         try {
             final XPathExpression xPathExpression = (XPathExpression)this.pool.borrowObject(expression);
             try {
                return callback.doWithExpression(xPathExpression);
             }
             finally {
                 this.pool.returnObject(expression, xPathExpression);
             }
         }
         catch (Exception e) {
             if (e instanceof RuntimeException) {
                 throw (RuntimeException)e;
             }
             if (e instanceof XPathExpressionException) {
                 throw (XPathExpressionException)e;
             }
             throw new IllegalStateException("Exception of type " + e.getClass().getName() + " is not expected", e);
         }
     }
     
     public <T> T evaluate(String expression, final Object item, final QName returnType) throws XPathExpressionException {
         return this.doWithExpression(expression, new XPathExpressionCallback<T>() {
             @SuppressWarnings("unchecked")
             public T doWithExpression(XPathExpression xPathExpression) throws XPathExpressionException {
                 return (T)xPathExpression.evaluate(item, returnType);
             }
         });
     }
     
     public interface XPathExpressionCallback<T> {
         T doWithExpression(XPathExpression xPathExpression) throws XPathExpressionException;
     }
     
     private class XPathExpressionFactory extends BaseKeyedPoolableObjectFactory {
         private final XPathFactory xPathFactory = XPathFactory.newInstance();
         
         @Override
         public synchronized Object makeObject(Object key) throws Exception {
             final String expression = (String)key;
             
             final XPath xPath = xPathFactory.newXPath();
             if (namespaceContext != null) {
                 xPath.setNamespaceContext(namespaceContext);
             }
             
             return xPath.compile(expression);
         }
     }
     
     public void clear() {
         pool.clear();
     }
 
     public void clearOldest() {
         pool.clearOldest();
     }
 
     public void close() throws Exception {
         pool.close();
     }
 
     public void evict() throws Exception {
         pool.evict();
     }
 
     public boolean getLifo() {
         return pool.getLifo();
     }
 
     public int getMaxActive() {
         return pool.getMaxActive();
     }
 
     public int getMaxIdle() {
         return pool.getMaxIdle();
     }
 
     public int getMaxTotal() {
         return pool.getMaxTotal();
     }
 
     public long getMaxWait() {
         return pool.getMaxWait();
     }
 
     public long getMinEvictableIdleTimeMillis() {
         return pool.getMinEvictableIdleTimeMillis();
     }
 
     public int getMinIdle() {
         return pool.getMinIdle();
     }
 
     public int getNumActive() {
         return pool.getNumActive();
     }
 
     public int getNumIdle() {
         return pool.getNumIdle();
     }
 
     public int getNumTestsPerEvictionRun() {
         return pool.getNumTestsPerEvictionRun();
     }
 
     public boolean getTestOnBorrow() {
         return pool.getTestOnBorrow();
     }
 
     public boolean getTestOnReturn() {
         return pool.getTestOnReturn();
     }
 
     public boolean getTestWhileIdle() {
         return pool.getTestWhileIdle();
     }
 
     public long getTimeBetweenEvictionRunsMillis() {
         return pool.getTimeBetweenEvictionRunsMillis();
     }
 
     public byte getWhenExhaustedAction() {
         return pool.getWhenExhaustedAction();
     }
 
     public void setLifo(boolean lifo) {
         pool.setLifo(lifo);
     }
 
     public void setMaxActive(int maxActive) {
         pool.setMaxActive(maxActive);
     }
 
     public void setMaxIdle(int maxIdle) {
         pool.setMaxIdle(maxIdle);
     }
 
     public void setMaxTotal(int maxTotal) {
         pool.setMaxTotal(maxTotal);
     }
 
     public void setMaxWait(long maxWait) {
         pool.setMaxWait(maxWait);
     }
 
     public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
         pool.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
     }
 
     public void setMinIdle(int poolSize) {
         pool.setMinIdle(poolSize);
     }
 
     public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
         pool.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
     }
 
     public void setTestOnBorrow(boolean testOnBorrow) {
         pool.setTestOnBorrow(testOnBorrow);
     }
 
     public void setTestOnReturn(boolean testOnReturn) {
         pool.setTestOnReturn(testOnReturn);
     }
 
     public void setTestWhileIdle(boolean testWhileIdle) {
         pool.setTestWhileIdle(testWhileIdle);
     }
 
     public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
         pool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
     }
 
     public void setWhenExhaustedAction(byte whenExhaustedAction) {
         pool.setWhenExhaustedAction(whenExhaustedAction);
     }
 }

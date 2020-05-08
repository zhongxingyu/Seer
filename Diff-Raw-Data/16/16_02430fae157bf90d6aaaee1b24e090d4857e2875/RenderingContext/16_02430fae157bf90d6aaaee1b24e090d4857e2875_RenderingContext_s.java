 /*
  * Copyright (c) 2011 for Jacek Bzdak
  *
  * This file is part of query builder.
  *
  * Query builder is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Query builder is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Query builder.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package cx.ath.jbzdak.sqlbuilder;
 
 import cx.ath.jbzdak.sqlbuilder.dialect.IdentifierQuotingStrategy;
 import cx.ath.jbzdak.sqlbuilder.dialect.QuotingManager;
 import cx.ath.jbzdak.sqlbuilder.dialect.config.DialectConfig;
 import cx.ath.jbzdak.sqlbuilder.expressionConfig.ExpressionConfigKey;
 import cx.ath.jbzdak.sqlbuilder.parameter.BoundParameter;
 
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created by: Jacek Bzdak
  */
 public class RenderingContext{
 
    LinkedList<IntermediateSQLFactory> parentExpressions = new LinkedList<IntermediateSQLFactory>();
 
    LinkedList<DialectConfig> overrideConfigs = new LinkedList<DialectConfig>();
 
    ContextInfo contextInfo;
 
    final ExpressionContext expressionContext;
 
    public RenderingContext(ExpressionContext expressionContext) {
       this.expressionContext = expressionContext;
    }
 
    public RenderingContext(SQLObject sqlObject) {
       this.expressionContext = sqlObject.getContext();
    }
 
    public List<IntermediateSQLFactory> getParentExpressions() {
       return Collections.unmodifiableList(parentExpressions);
    }
 
    public IntermediateSQLFactory pop() {
       contextInfo = new ContextInfo();
       return parentExpressions.pop();
    }
 
    public void push(IntermediateSQLFactory sqlFactory) {
       contextInfo = new ContextInfo();
       parentExpressions.push(sqlFactory);
    }
 
    public QuotingManager getQuotingManager() {
       return getDialect().getQuotingManager();
    }
 
    public IntermediateSQLFactory getRootExpression(){
       return parentExpressions.get(0);
    }
 
    public IntermediateSQLFactory getParentExpression(){
       int size = parentExpressions.size();
       if(size <= 1){
          return null;
       }
       return parentExpressions.get(size -2);
    }
 
    public DialectConfig getDialectConfig() {
       return expressionContext.getDialectConfig();
    }
 
    public List<Select> getSelects() {
       return contextInfo.getSelects();
    }
 
    public Select getParentSelect() {
       return contextInfo.getParentSelect();
    }
 
    public String quoteIdentifier(CharSequence ident, IdentifierQuotingStrategy strategy, IdenitfierPart idenitfierPart) {
       return getQuotingManager().quoteIdentifier(ident, strategy, idenitfierPart);
    }
 
    public String quoteDate(Date quote) {
       return getQuotingManager().quoteDate(quote);
    }
 
    /**
     * Return fist found parent of class.
     * @param aClass
     * @return
     */
    public IntermediateSQLFactory findParentOfClass(Class aClass){
       for (IntermediateSQLFactory parentExpression : parentExpressions) {
          if(aClass.isInstance(parentExpression)){
             return parentExpression;
          }
       }
       return null;
    }
 
 
    @Deprecated()
    /**
     * @deprecated You should not use this method, rather use delegates from instances of this class.
     * For example rather use {@code this.getDialectConfig()} than {@code this.getDialect().getDialectConfig()}
     */
    public Dialect getDialect() {
       return expressionContext.getDialect();
    }
 
 
    public String quoteString(CharSequence quote) {
       return getQuotingManager().quoteString(quote);
    }
 
    public ExpressionContext getExpressionContext() {
       return expressionContext;
    }
 
    public String replaceParams(CharSequence queryPart){
       if(queryPart == null){
          return null;
       }
       Pattern pattern = (Pattern) expressionContext.expressionConfig.get(ExpressionConfigKey.PARAMETER_REGEXP_PATTERN);
       Matcher matcher = pattern.matcher(queryPart);
       StringBuffer stringBuffer = new StringBuffer();
       while (matcher.find()){
          Map<String, BoundParameter> boundParameters = expressionContext.getBoundParameters();
         BoundParameter boundParameter = boundParameters.get(matcher.group(1));
          if(boundParameter == null){
            throw new UnvaluedParameter("No value set for parameter '" + matcher.group(1) + "'");
          }
          matcher.appendReplacement(stringBuffer, MiscUtils.toSQL(this, boundParameter));
       }
       matcher.appendTail(stringBuffer);
       return stringBuffer.toString();
    }
 
    private class ContextInfo{
 
       List<Select> selects;
 
       public List<Select> getSelects() {
          if (selects == null) {
             selects = new ArrayList<Select>();
             for (IntermediateSQLFactory parentExpression : parentExpressions) {
                if (parentExpression instanceof Select) {
                   Select select = (Select) parentExpression;
                   selects.add(select);
                }
             }
          }
          return selects;
       }
 
       public Select getParentSelect(){
          if(getSelects().isEmpty()){
             return null;
          }
          return getSelects().get(getSelects().size() -1);
       }
    }
 
 
 }

 package trumpet.maven.util;
 
 import java.io.StringReader;
 import java.net.URL;
 
 import org.antlr.stringtemplate.StringTemplate;
 import org.antlr.stringtemplate.StringTemplateGroup;
 import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
 import org.apache.commons.lang3.StringUtils;
 import org.skife.jdbi.v2.StatementContext;
 import org.skife.jdbi.v2.tweak.StatementLocator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.io.Resources;
 import com.nesscomputing.migratory.loader.LoaderManager;
 
 public class TemplatingStatementLocator implements StatementLocator
 {
     private static final Logger LOG = LoggerFactory.getLogger(TemplatingStatementLocator.class);
 
     private final LoaderManager loaderManager;
     private final String prefix;
 
     public TemplatingStatementLocator(final String prefix, final LoaderManager loaderManager)
     {
         this.prefix = prefix;
         this.loaderManager = loaderManager;
     }
 
     @Override
     public String locate(final String statementName, final StatementContext context) throws Exception
     {
         if (StringUtils.isEmpty(statementName)) {
             throw new IllegalStateException("Statement Name can not be empty/null!");
         }
 
         if (statementName.charAt(0) == '#') {
             // Multiple templates can be in a string template group. In that case, the name is #<template-group:<statement name>
             final String [] statementNames = StringUtils.split(statementName.substring(1), ":");
 
             final String location = prefix + statementNames[0] + ".st";
 
             LOG.trace("Loading SQL: {}", location);
             final URL locationUrl = Resources.getResource(this.getClass(), location);
 
             if (locationUrl == null) {
                throw new IllegalArgumentException("Location '" + locationUrl + "' does not exist!");
             }
             final String contents = loaderManager.loadFile(locationUrl.toURI());
 
             if (statementNames.length == 1) {
                 final StringTemplate template = new StringTemplate(contents, AngleBracketTemplateLexer.class);
                 template.setAttributes(context.getAttributes());
                 final String sql = template.toString();
 
                 LOG.trace("SQL: {}", sql);
                 return sql;
             }
             else {
                 final StringTemplateGroup group = new StringTemplateGroup(new StringReader(contents), AngleBracketTemplateLexer.class);
                 LOG.trace("Found {} in {}", group.getTemplateNames(), locationUrl);
 
                 final StringTemplate template = group.getInstanceOf(statementNames[1]);
                 template.setAttributes(context.getAttributes());
                 final String sql = template.toString();
 
                 LOG.trace("SQL: {}", sql);
                 return sql;
             }
         }
         else {
             return statementName;
         }
     }
 }

 /*
                         QueryJ
 
     Copyright (C) 2002-today  Jose San Leandro Armendariz
                               chous@acm-sl.org
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: jose.sanleandro@acm-sl.com
 
  ******************************************************************************
  *
  * Filename: PerTableTemplatesTest.java
  *
  * Author: Jose San Leandro Armendariz (chous)
  *
  * Description: Cucumber tests for per-table templates.
  *
  * Date: 2013/05/04
  * Time: 9:33 AM
  *
  */
 package cucumber.templates;
 
 /*
  * Importing project classes.
  */
 import org.acmsl.queryj.customsql.Property;
 import org.acmsl.queryj.customsql.PropertyElement;
 import org.acmsl.queryj.customsql.PropertyRef;
 import org.acmsl.queryj.customsql.PropertyRefElement;
 import org.acmsl.queryj.customsql.Result;
 import org.acmsl.queryj.customsql.ResultElement;
 import org.acmsl.queryj.metadata.DecoratorFactory;
 import org.acmsl.queryj.templates.BasePerCustomResultTemplate;
 import org.acmsl.queryj.templates.BasePerCustomResultTemplateFactory;
 import org.acmsl.queryj.templates.BasePerCustomResultTemplateGenerator;
 import org.acmsl.queryj.templates.dao.CustomResultSetExtractorTemplateFactory;
 import org.acmsl.queryj.templates.dao.CustomResultSetExtractorTemplateGenerator;
 import org.acmsl.queryj.templates.valueobject.CustomBaseValueObjectTemplateFactory;
 import org.acmsl.queryj.templates.valueobject.CustomBaseValueObjectTemplateGenerator;
 import org.acmsl.queryj.templates.valueobject.CustomValueObjectFactoryTemplateFactory;
 import org.acmsl.queryj.templates.valueobject.CustomValueObjectFactoryTemplateGenerator;
 import org.acmsl.queryj.templates.valueobject.CustomValueObjectImplTemplateFactory;
 import org.acmsl.queryj.templates.valueobject.CustomValueObjectImplTemplateGenerator;
 import org.acmsl.queryj.templates.valueobject.CustomValueObjectTemplateFactory;
 import org.acmsl.queryj.templates.valueobject.CustomValueObjectTemplateGenerator;
 import org.acmsl.queryj.tools.QueryJBuildException;
 
 /*
  * Importing ACM-SL Commons classes.
  */
 import org.acmsl.commons.logging.UniqueLogFactory;
 
 /*
  * Importing Cucumber classes.
  */
 import cucumber.api.DataTable;
 import cucumber.api.java.en.Given;
 import cucumber.api.java.en.Then;
 import cucumber.api.java.en.When;
 
 /*
  * Importing Apache Commons Logging classes.
  */
 import org.apache.commons.logging.LogFactory;
 
 /*
  * Importing Jetbrains Annotations.
  */
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /*
  * Importing JUnit classes.
  */
 import org.junit.Assert;
 
 /*
  * Importing JDK classes.
  */
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Cucumber tests for per-custom-results templates.
  * @author <a href="chous@acm-sl.org">Jose San Leandro</a>
  * @since 2013/05/04
  */
 public class PerCustomResultTemplatesTest
     extends AbstractTemplatesTest<BasePerCustomResultTemplateGenerator, BasePerCustomResultTemplateFactory>
 {
     /**
      * The results.
      */
     private Map<String, Result> m__mResults;
 
     /**
      * The properties.
      */
     private Map<String, Property> m__mProperties;
 
     /**
      * Creates an instance.
      */
     public PerCustomResultTemplatesTest()
     {
         immutableSetResults(new HashMap<String, Result>());
         immutableSetProperties(new HashMap<String, Property>());
 
         GENERATOR_MAPPINGS.put("CustomResultSetExtractor", new CustomResultSetExtractorTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("CustomResultSetExtractor", CustomResultSetExtractorTemplateFactory.getInstance());
         // vo
         GENERATOR_MAPPINGS.put("CustomBaseValueObject", new CustomBaseValueObjectTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("CustomBaseValueObject", CustomBaseValueObjectTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("CustomValueObject", new CustomValueObjectTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("CustomValueObject", CustomValueObjectTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("CustomValueObjectFactory", new CustomValueObjectFactoryTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("CustomValueObjectFactory", CustomValueObjectFactoryTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("CustomValueObjectImpl", new CustomValueObjectImplTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("CustomValueObjectImpl", CustomValueObjectImplTemplateFactory.getInstance());
     }
 
     /**
      * Specifies the custom results.
      * @param results the results.
      */
     protected final void immutableSetResults(@NotNull final Map<String, Result> results)
     {
         m__mResults = results;
     }
 
     /**
      * Specifies the custom results.
      * @param results the results.
      */
     @SuppressWarnings("unused")
     protected void setResults(@NotNull final Map<String, Result> results)
     {
         immutableSetResults(results);
     }
 
     /**
      * Retrieves the custom results.
      * @return such information.
      */
     @NotNull
     protected Map<String, Result> getResults()
     {
         return m__mResults;
     }
 
     /**
      * Specifies the properties.
      * @param properties the properties.
      */
     protected final void immutableSetProperties(@NotNull final Map<String, Property> properties)
     {
         m__mProperties = properties;
     }
 
     /**
      * Specifies the properties.
      * @param properties the properties.
      */
     @SuppressWarnings("unused")
     protected void setProperties(@NotNull final Map<String, Property> properties)
     {
         immutableSetProperties(properties);
     }
 
     /**
      * Retrieves the properties.
      * @return such information.
      */
     @NotNull
     protected Map<String, Property> getProperties()
     {
         return m__mProperties;
     }
 
     /**
      * Defines the input custom results based on the information provided by the
      * feature.
      * @param resultInfo the information about the results.
      */
     @SuppressWarnings("unused")
     @Given("^the following custom results:$")
     public void defineInputCustomResults(@NotNull final DataTable resultInfo)
     {
         defineInputCustomResults(resultInfo, getResults());
     }
 
     /**
      * Defines the input custom results based on the information provided by the
      * feature.
      * @param resultInfo the information about the results.
      * @param results the result collection.
      */
     protected void defineInputCustomResults(
         @NotNull final DataTable resultInfo, @NotNull final Map<String, Result> results)
     {
         @NotNull final List<Map<String, String>> resultEntries = resultInfo.asMaps();
 
         @NotNull Result currentResult;
 
         for (@NotNull final Map<String, String> resultEntry: resultEntries)
         {
             currentResult = convertToResult(resultEntry);
 
             results.put(currentResult.getId(), currentResult);
         }
     }
 
     /**
      * Defines the properties from the feature.
      * @param propertyInfo the property information.
      */
     @SuppressWarnings("unused")
     @Given("^the following properties:$")
     public void defineInputProperties(@NotNull final DataTable propertyInfo)
     {
         defineInputProperties(propertyInfo, getProperties(), getResults());
     }
 
     /**
      * Defines the properties from the feature.
      * @param propertyInfo the property information.
      * @param properties the properties.
      * @param results the results.
      */
     protected void defineInputProperties(
         @NotNull final DataTable propertyInfo,
         @NotNull final Map<String, Property> properties,
         @NotNull final Map<String, Result> results)
     {
         @NotNull final List<Map<String, String>> propertyEntries = propertyInfo.asMaps();
 
         @NotNull Property property;
         @Nullable Result result;
 
         for (@NotNull final Map<String, String> propertyEntry: propertyEntries)
         {
             property = convertToProperty(propertyEntry);
 
             properties.put(property.getId(), property);
             result = results.get(propertyEntry.get("custom-result-id"));
 
             Assert.assertNotNull("Invalid custom result for property : " + property, result);
             result.add(new PropertyRefElement(property.getId()));
         }
     }
 
     /**
      * Checks the generated file compiles.
      * @param outputName the name of the output file.
      */
     @SuppressWarnings("unused")
     @Then("^the generated per-custom-result (.*) file compiles successfully")
     public void checkGeneratedFileCompiles(@NotNull final String outputName)
     {
         checkGeneratedFilesCompile(outputName, getOutputFiles());
     }
 
     /**
      * Retrieves the {@link DecoratorFactory} instance using given generator.
      * @param generator the generator to use.
      * @return the decorator factory.
      */
     @NotNull
     @Override
     protected DecoratorFactory retrieveDecoratorFactory(@NotNull final BasePerCustomResultTemplateGenerator generator)
     {
         return generator.getDecoratorFactory();
     }
 
     /**
      * Generates a file with the information from the feature.
      * @param template the template.
      * @param engine the engine.
      */
     @SuppressWarnings("unused")
     @When("^I generate with per-custom-result (.*)\\.stg for (.*)$")
     public void generateFile(@NotNull final String template, @NotNull final String engine)
     {
         generateFile(template, engine, getResults(), getProperties(), getOutputFiles());
     }
 
     /**
      * Generates a file with the information from the feature.
      * @param templateName the template.
      * @param engine the engine.
      * @param results the results.
      * @param properties the properties.
      * @param outputFiles the output files.
      */
     @SuppressWarnings("unchecked")
     protected void generateFile(
         @NotNull final String templateName,
         @NotNull final String engine,
         @NotNull final Map<String, Result> results,
         @NotNull final Map<String, Property> properties,
         @NotNull final Map<String, File> outputFiles)
     {
         @Nullable final BasePerCustomResultTemplateGenerator generator =
             retrieveTemplateGenerator(templateName);
 
         Assert.assertNotNull("No template generator found for " + templateName, generator);
 
         for (@NotNull final Result currentResult : results.values())
         {
             @Nullable final BasePerCustomResultTemplateFactory templateFactory = retrieveTemplateFactory(templateName);
 
             Assert.assertNotNull("No template factory found for " + templateName, templateFactory);
 
             @Nullable final BasePerCustomResultTemplate template =
                 templateFactory.createTemplate(
                     retrieveCustomSqlProvider(currentResult, retrieveProperties(currentResult, properties)),
                     retrieveMetadataManager(engine),
                     retrieveDecoratorFactory(generator),
                     "com.foo.bar.dao",
                     "com.foo.bar",
                     "acme", // repository
                     "", // header
                     false, // marker
                     false, // jmx
                     "java:comp/env/db",
                     false, // disable generation timestamps
                     false, // disable NotNull annotations
                     true, // disable checkThread.org annotations
                     currentResult);
 
             Assert.assertNotNull("No template found for " + templateName, template);
 
             @Nullable File outputDir = null;
 
             try
             {
                 rootFolder.create();
                 outputDir = rootFolder.newFolder("dao");
             }
             catch (@NotNull final IOException ioException)
             {
                 Assert.fail(ioException.getMessage());
             }
 
 //            Assert.assertTrue("Cannot create folder: " + outputDir.getAbsolutePath(), outputDir.mkdirs());
 
             UniqueLogFactory.initializeInstance(LogFactory.getLog(PerCustomResultTemplatesTest.class));
 
             try
             {
                 generator.write(
                     template,
                     outputDir,
                     rootFolder.getRoot(),
                     Charset.defaultCharset());
             }
             catch (@NotNull final IOException ioException)
             {
                 Assert.fail(ioException.getMessage());
             }
             catch (@NotNull final QueryJBuildException queryjBuildException)
             {
                 Assert.fail(queryjBuildException.getMessage());
             }
 
             outputFiles.put(
                 currentResult.getId(),
                 new File(outputDir, generator.retrieveTemplateFileName(template.getTemplateContext())));
         }
     }
 
     /**
      * Retrieves the properties for given result.
      * @param currentResult the result.
      * @param properties the properties.
      * @return the list of properties of given result.
      */
     protected List<Property> retrieveProperties(
         @NotNull final Result currentResult, @NotNull final Map<String, Property> properties)
     {
         @NotNull final List<Property> result = new ArrayList<Property>();
 
         @Nullable Property currentProperty;
 
         for (@NotNull final PropertyRef propertyRef : currentResult.getPropertyRefs())
         {
             currentProperty = properties.get(propertyRef.getId());
 
             if (currentProperty != null)
             {
                 result.add(currentProperty);
             }
         }
 
         return result;
     }
 
     /**
      * Converts given custom result information to a {@link Result}.
      * @param resultEntry the result information.
      * @return the {@link Result} instance.
      */
     @NotNull
     protected Result convertToResult(@NotNull final Map<String, String> resultEntry)
     {
         return new ResultElement(resultEntry.get("id"), resultEntry.get("class"), resultEntry.get("matches"));
     }
 
 
     /**
      * Converts given custom result information to a {@link Property}.
      * @param propertyEntry the property information.
      * @return the {@link Property} instance.
      */
     @NotNull
     protected Property convertToProperty(@NotNull final Map<String, String> propertyEntry)
     {
         return
             new PropertyElement(
                 propertyEntry.get("id"),
                propertyEntry.get("column-name"),
                 Integer.valueOf(propertyEntry.get("index")),
                 propertyEntry.get("type"),
                 Boolean.valueOf(propertyEntry.get("nullable")));
     }
 }

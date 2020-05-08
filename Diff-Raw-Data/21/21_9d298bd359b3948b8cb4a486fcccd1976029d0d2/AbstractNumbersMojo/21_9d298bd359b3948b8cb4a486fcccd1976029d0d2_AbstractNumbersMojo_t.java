 package com.likeness.maven.plugins.numbers;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang3.tuple.Pair;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.likeness.maven.plugins.numbers.beans.DateDefinition;
 import com.likeness.maven.plugins.numbers.beans.IWFEnum;
 import com.likeness.maven.plugins.numbers.beans.MacroDefinition;
 import com.likeness.maven.plugins.numbers.beans.NumberDefinition;
 import com.likeness.maven.plugins.numbers.beans.PropertyGroup;
 import com.likeness.maven.plugins.numbers.beans.StringDefinition;
 import com.likeness.maven.plugins.numbers.util.Log;
 import com.pyx4j.log4j.MavenLogAppender;
 
 /**
  * Base code for all the mojos.
  */
 public abstract class AbstractNumbersMojo extends AbstractMojo
 {
     /**
      * The maven project (effective pom).
      * @parameter expression="${project}"
      * @required
      * @readonly
     */
     protected MavenProject project;
 
     /**
      * Skip the plugin execution.
      *
      * @parameter default-value="false"
      */
     protected boolean skip = false;
 
     /**
      * Default action on duplicate properties.
      *
      * @parameter default-value="fail"
      */
     protected String onDuplicateProperty = "fail";
 
     /**
      * Default action on missing properties.
      *
      * @parameter default-value="fail"
      */
     protected String onMissingProperty = "fail";
 
     /**
      * Which groups to activate for this plugin run.
      *
      * @parameter
      */
     protected String[] activeGroups;
 
     /**
      * Property groups.
      *
      * @parameter
      */
     protected PropertyGroup[] propertyGroups;
 
     /**
      * Numbers.
      *
      * @parameter
      * @name="numbers"
      */
     protected NumberDefinition[] numbers;
 
     /**
      * Strings.
      *
      * @parameter
      * @name="strings"
      */
     protected StringDefinition[] strings;
 
     /**
      * Dates.
      *
      * @parameter
      * @name="dates"
      */
     protected DateDefinition[] dates;
 
     /**
      * Macros.
      *
      * @parameter
      * @name="macros"
      */
     protected MacroDefinition[] macros;
 
     protected final Log LOG = Log.findLog();
 
     protected final PropertyCache propertyCache = new PropertyCache();
     private final Map<String, String> props = Maps.newHashMap();
 
     protected List<NumberField> numberFields = null;
 
     private boolean isSnapshot;
 
     public void execute() throws MojoExecutionException, MojoFailureException
     {
         MavenLogAppender.startPluginLog(this);
 
         isSnapshot = project.getArtifact().isSnapshot();
         LOG.debug("Project is a %s.", isSnapshot ? "snapshot" : "release");
 
         try {
             if (skip) {
                 LOG.debug("Skipping execution!");
             }
             else {
                 doExecute();
             }
         }
         catch (MojoExecutionException me) {
             throw me;
         }
         catch (MojoFailureException mfe) {
             throw mfe;
         }
         catch (Exception e) {
             throw new MojoExecutionException("While running mojo: ", e);
         }
         finally {
             LOG.debug("Ended %s mojo run!", this.getClass().getSimpleName());
             MavenLogAppender.endPluginLog(this);
         }
     }
 
     /**
      * Subclasses need to implement this method.
      */
     protected abstract void doExecute() throws Exception;
 
     protected void loadPropertyElements()
         throws Exception
     {
        final List<PropertyElement> propertyElements = Lists.newArrayList();

         numberFields = NumberField.createNumbers(propertyCache, numbers);
         propertyElements.addAll(numberFields);
         propertyElements.addAll(StringField.createStrings(propertyCache, strings));
         propertyElements.addAll(DateField.createDates(propertyCache, dates));
         propertyElements.addAll(MacroField.createMacros(propertyCache, macros));
 
         for (final PropertyElement pe : propertyElements) {
             final String value = pe.getPropertyValue();
             props.put(pe.getPropertyName(), value);
 
             if (pe.isExport()) {
                 project.getProperties().setProperty(pe.getPropertyName(), Objects.firstNonNull(value, ""));
                 LOG.debug("Exporting Property name: %s, value: %s", pe.getPropertyName(), value);
             }
             else {
                 LOG.debug("Property name: %s, value: %s", pe.getPropertyName(), Objects.firstNonNull(value, "<null>"));
             }
         }
 
         // Now generate the property groups.
        final Map<String, Pair<PropertyGroup, List<PropertyElement>>> propertyPairs = Maps.newHashMap();
 
         final Set<String> propertyNames = Sets.newHashSet();
 
 
         if (propertyGroups != null) {
             for (PropertyGroup propertyGroup : propertyGroups) {
                 final List<PropertyElement> propertyFields = PropertyField.createProperties(props, propertyGroup);
                propertyPairs.put(propertyGroup.getId(), Pair.of(propertyGroup, propertyFields));
             }
         }
 
         if (activeGroups != null) {
             for (String activeGroup : activeGroups) {
                final Pair<PropertyGroup, List<PropertyElement>> propertyElement = propertyPairs.get(activeGroup);
                 Preconditions.checkState(propertyElement != null, "activated group '%s' does not exist", activeGroup);
 
                 final PropertyGroup propertyGroup = propertyElement.getLeft();
                 if ((propertyGroup.isActiveOnRelease() && !isSnapshot) || (propertyGroup.isActiveOnSnapshot() && isSnapshot)) {
                     for (final PropertyElement pe : propertyElement.getRight()) {
                         final String value = pe.getPropertyValue();
                         final String propertyName = pe.getPropertyName();
                         IWFEnum.checkState(propertyGroup.getOnDuplicateProperty(), !propertyNames.contains(propertyName), "property name '" + propertyName + "'");
                         propertyNames.add(propertyName);
 
                         project.getProperties().setProperty(propertyName, Objects.firstNonNull(value, ""));
                     }
                 }
                 else {
                     LOG.debug("Skipping property group %s: Snapshot: %b, onSnapshot: %b, onRelease: %b", activeGroup, isSnapshot, propertyGroup.isActiveOnSnapshot(), propertyGroup.isActiveOnRelease());
                 }
             }
         }
     }
 }

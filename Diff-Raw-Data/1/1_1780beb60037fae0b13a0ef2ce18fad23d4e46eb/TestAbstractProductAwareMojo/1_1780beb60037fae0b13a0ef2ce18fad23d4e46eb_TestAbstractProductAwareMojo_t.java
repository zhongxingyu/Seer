 package com.atlassian.maven.plugins.amps;
 
 import junit.framework.TestCase;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.model.Build;
 import org.apache.maven.model.Plugin;
 
 import java.lang.reflect.Field;
 
 public class TestAbstractProductAwareMojo extends TestCase
 {
     public void testGetProductId() throws MojoExecutionException
     {
         assertEquals("foo", new SomeMojo("foo").getProductId());
     }
 
     public void testGetProductIdFromParam() throws MojoExecutionException, NoSuchFieldException, IllegalAccessException
     {
         SomeMojo mojo = new SomeMojo("foo");
         setPrivateField(mojo, "product", "bar");
         assertEquals("bar", mojo.getProductId());
     }
 
     public void testGetDefaultProductId() throws MojoExecutionException, NoSuchFieldException, IllegalAccessException
     {
         SomeMojo mojo = new SomeMojo(null);
         assertEquals("refapp", mojo.getProductId());
     }
 
     private void setPrivateField(Object object, String fieldName, Object value)
             throws IllegalAccessException
     {
         Field field = null;
         Class clazz = object.getClass();
         while (clazz != Object.class)
         {
             for (Field f : clazz.getDeclaredFields())
             {
                 if (fieldName.equals(f.getName()))
                 {
                     field = f;
                 }
             }
             clazz = clazz.getSuperclass();
         }
 
 
         assertNotNull(field);
 
         field.setAccessible(true);
         field.set(object, value);
     }
 
     public void testGetProductIdFromProject() throws MojoExecutionException, NoSuchFieldException, IllegalAccessException
     {
         SomeMojo mojo = new SomeMojo(null);
         MavenProject proj = new MavenProject();
         Build build = new Build();
         proj.setBuild(build);
         Plugin plugin = new Plugin();
         build.addPlugin(plugin);
         plugin.setGroupId("com.atlassian.maven.plugins");
         plugin.setArtifactId("maven-confluence-plugin");
         setPrivateField(mojo, "project", proj);
         assertEquals("confluence", mojo.getProductId());
     }
 
     public static class SomeMojo extends AbstractProductAwareMojo
     {
         private final String defaultProductId;
 
         public SomeMojo(String defaultProductId)
         {
             this.defaultProductId = defaultProductId;
         }
 
         @Override
         protected String getDefaultProductId() throws MojoExecutionException
         {
             return defaultProductId;
         }
 
         public void execute() throws MojoExecutionException, MojoFailureException
         {
         }
     }
 }

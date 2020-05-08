 // Copyright (C) 2000 Paco Gomez
 // Copyright (C) 2000, 2001, 2002, 2003, 2004, 2005 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.common;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 import java.util.Properties;
 
 
 /**
  * Extend {@link java.util.Properties} to add typesafe accessors.
  * Has an optional associated file.
  *
  * @author Philip Aston
  * @version $Revision$
  * @see net.grinder.script.Grinder.ScriptContext#getProperties
  */
 public class GrinderProperties extends Properties {
   private static final String DEFAULT_FILENAME = "grinder.properties";
 
   private PrintWriter m_errorWriter = new PrintWriter(System.err, true);
   private final File m_file;
 
   /**
    * Construct an empty GrinderProperties with no associated file.
    */
   public GrinderProperties() {
     m_file = null;
   }
 
   /**
    * Construct a GrinderProperties, reading initial values from the
    * specified file. System properties beginning with
    * "<code>grinder.</code>"are also added to allow values to be
   * overriden on the command line.
    * @param file The file to read the properties from.
    * <code>null</code> => use grinder.properties.
    *
    * @exception PersistenceException If an error occurs reading from
    * the file.
    */
   public GrinderProperties(File file) throws PersistenceException {
     m_file = file != null ? file : new File(DEFAULT_FILENAME);
 
     if (m_file.exists()) {
       try {
         final InputStream propertiesInputStream = new FileInputStream(m_file);
 
         load(propertiesInputStream);
 
         propertiesInputStream.close();
       }
       catch (IOException e) {
         throw new PersistenceException(
           "Error loading properties file '" + m_file.getPath() + '\'', e);
       }
     }
 
     final Enumeration systemProperties =
       System.getProperties().propertyNames();
 
     while (systemProperties.hasMoreElements()) {
       final String name = (String)systemProperties.nextElement();
 
       if (name.startsWith("grinder.")) {
         put(name, System.getProperty(name));
       }
     }
   }
 
   /**
    * Accessor for unit tests.
    */
   File getAssociatedFile() {
     return m_file;
   }
 
   /**
    * Save our properties to our file.
    *
    * @exception PersistenceException If there is no file associated
    * with this {@link GrinderProperties} or an I/O exception occurs..
    */
   public final void save() throws PersistenceException {
 
     if (m_file == null) {
       throw new PersistenceException("No associated file");
     }
 
     try {
       final OutputStream outputStream = new FileOutputStream(m_file);
       store(outputStream, generateFileHeader());
       outputStream.close();
     }
     catch (IOException e) {
       throw new PersistenceException(
         "Error writing properties file '" + m_file.getPath() + '\'', e);
     }
   }
 
   /**
    * Save a single property to our file.
    *
    * @param name Property name.
    * @exception PersistenceException If there is no file associated with this
    * {@link GrinderProperties} or an I/O exception occurs..
    */
   public final void saveSingleProperty(String name)
     throws PersistenceException {
 
     if (m_file == null) {
       throw new PersistenceException("No associated file");
     }
 
     try {
       final Properties properties = new Properties();
 
       try {
         final InputStream inputStream = new FileInputStream(m_file);
         properties.load(inputStream);
         inputStream.close();
       }
       catch (IOException e) {
         // Can't read the file, maybe its not there. Ignore.
       }
 
       final OutputStream outputStream = new FileOutputStream(m_file);
       properties.setProperty(name, getProperty(name));
       properties.store(outputStream, generateFileHeader());
       outputStream.close();
     }
     catch (IOException e) {
       throw new PersistenceException(
         "Error writing properties file '" + m_file.getPath() + '\'', e);
     }
   }
 
   private String generateFileHeader() {
     return "Properties file for The Grinder";
   }
 
   /**
    * Set a writer to report warnings to.
    *
    * @param writer The writer.
    */
   public final void setErrorWriter(PrintWriter writer) {
     m_errorWriter = writer;
   }
 
   /**
    * Return a new GrinderProperties that contains the subset of our
    * Properties which begin with the specified prefix.
    * @param prefix The prefix.
    *
    * @return The subset.
    */
   public final synchronized GrinderProperties
     getPropertySubset(String prefix) {
     final GrinderProperties result = new GrinderProperties();
 
     final Enumeration propertyNames = propertyNames();
 
     while (propertyNames.hasMoreElements()) {
       final String name = (String)propertyNames.nextElement();
 
       if (name.startsWith(prefix)) {
         result.setProperty(name.substring(prefix.length()),
                            getProperty(name));
       }
     }
 
     return result;
   }
 
   /**
    * Get the value of the property with the given name, return the
    * value as an <code>int</code>.
    * @param propertyName The property name.
    * @param defaultValue The value to return if a property with the
    * given name does not exist or is not an integer.
    *
    * @return The value.
    */
   public final int getInt(String propertyName, int defaultValue) {
     final String s = getProperty(propertyName);
 
     if (s != null) {
       try {
         return Integer.parseInt(s);
       }
       catch (NumberFormatException e) {
         m_errorWriter.println("Warning, property '" + propertyName +
                               "' does not specify an integer value");
       }
     }
 
     return defaultValue;
   }
 
   /**
    * Set the property with the given name to an <code>int</code>
    * value.
    * @param propertyName The property name.
    * @param value The value to set.
    */
   public final void setInt(String propertyName, int value) {
     setProperty(propertyName, Integer.toString(value));
   }
 
 
   /**
    * Get the value of the property with the given name, return the
    * value as a <code>long</code>.
    * @param propertyName The property name.
    * @param defaultValue The value to return if a property with the
    * given name does not exist or is not a long.
    *
    * @return The value.
    */
   public final long getLong(String propertyName, long defaultValue) {
     final String s = getProperty(propertyName);
 
     if (s != null) {
       try {
         return Long.parseLong(s);
       }
       catch (NumberFormatException e) {
         m_errorWriter.println("Warning, property '" + propertyName +
                               "' does not specify an integer value");
       }
     }
 
     return defaultValue;
   }
 
   /**
    * Set the property with the given name to a <code>long</code>
    * value.
    * @param propertyName The property name.
    * @param value The value to set.
    */
   public final void setLong(String propertyName, long value) {
     setProperty(propertyName, Long.toString(value));
   }
 
   /**
    * Get the value of the property with the given name, return the
    * value as a <code>short</code>.
    * @param propertyName The property name.
    * @param defaultValue The value to return if a property with the
    * given name does not exist or is not a short.
    *
    * @return The value.
    */
   public final short getShort(String propertyName, short defaultValue) {
     final String s = getProperty(propertyName);
 
     if (s != null) {
       try {
         return Short.parseShort(s);
       }
       catch (NumberFormatException e) {
         m_errorWriter.println("Warning, property '" + propertyName +
                               "' does not specify a short value");
       }
     }
 
     return defaultValue;
   }
 
   /**
    * Set the property with the given name to a <code>short</code>
    * value.
    * @param propertyName The property name.
    * @param value The value to set.
    */
   public final void setShort(String propertyName, short value) {
     setProperty(propertyName, Short.toString(value));
   }
 
   /**
    * Get the value of the property with the given name, return the
    * value as a <code>double</code>.
    * @param propertyName The property name.
    * @param defaultValue The value to return if a property with the
    * given name does not exist or is not a double.
    *
    * @return The value.
    */
   public final double getDouble(String propertyName, double defaultValue) {
     final String s = getProperty(propertyName);
 
     if (s != null) {
       try {
         return Double.parseDouble(s);
       }
       catch (NumberFormatException e) {
         m_errorWriter.println("Warning, property '" + propertyName +
                               "' does not specify a double value");
       }
     }
 
     return defaultValue;
   }
 
   /**
    * Set the property with the given name to a <code>double</code>
    * value.
    * @param propertyName The property name.
    * @param value The value to set.
    */
   public final void setDouble(String propertyName, double value) {
     setProperty(propertyName, Double.toString(value));
   }
 
   /**
    * Get the value of the property with the given name, return the
    * value as a <code>boolean</code>.
    * @param propertyName The property name.
    * @param defaultValue The value to return if a property with the
    * given name does not exist.
    *
    * @return The value.
    */
   public final boolean getBoolean(String propertyName, boolean defaultValue) {
     final String s = getProperty(propertyName);
 
     if (s != null) {
       return Boolean.valueOf(s).booleanValue();
     }
 
     return defaultValue;
   }
 
   /**
    * Set the property with the given name to a <code>boolean</code>
    * value.
    * @param propertyName The property name.
    * @param value The value to set.
    */
   public final void setBoolean(String propertyName, boolean value) {
     setProperty(propertyName, String.valueOf(value));
   }
 
   /**
    * Get the value of the property with the given name, return the
    * value as a <code>File</code>.
    * @param propertyName The property name.
    * @param defaultValue The value to return if a property with the
    * given name does not exist.
    *
    * @return The value.
    */
   public final File getFile(String propertyName, File defaultValue) {
     final String s = getProperty(propertyName);
 
     if (s != null) {
       return new File(s);
     }
 
     return defaultValue;
   }
 
   /**
    * Set the property with the given name to a <code>File</code>
    * value.
    * @param propertyName The property name.
    * @param value The value to set.
    */
   public final void setFile(String propertyName, File value) {
     setProperty(propertyName, value.getPath());
   }
 
   /**
    * Exception indicating a problem in persisting properties.
    */
   public static final class PersistenceException extends GrinderException {
     private PersistenceException(String message) {
       super(message);
     }
 
     private PersistenceException(String message, Throwable t) {
       super(message, t);
     }
   }
 }

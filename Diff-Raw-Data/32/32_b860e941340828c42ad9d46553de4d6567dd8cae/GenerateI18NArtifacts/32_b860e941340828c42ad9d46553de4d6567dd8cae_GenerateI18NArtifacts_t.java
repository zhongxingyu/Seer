 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.mavenplugin;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProjectHelper;
 
 import org.codehaus.plexus.archiver.Archiver;
 import org.codehaus.plexus.archiver.ArchiverException;
 import org.codehaus.plexus.archiver.manager.ArchiverManager;
 import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Set;
 
 /**
  * This goal generates default i18n jars from a specified folder.
  *
  * @author                        martin.scholl@cismet.de
  * @version                       $Revision$, $Date$
  * @goal                          generate-i18n
  * @phase                         prepare-package
  * @requiresDependencyResolution  runtime
  */
 public final class GenerateI18NArtifacts extends AbstractCidsMojo {
 
     //~ Static fields/initializers ---------------------------------------------
 
     public static final String EXT_DEFAULT = "jar"; // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     /**
      * Whether to skip the execution of this mojo.
      *
      * @parameter  expression="${cids.generate-i18n.skip}" default-value="false"
      * @required   false
      */
     private transient Boolean skip;
 
     /**
      * The <code>default.i18n.locale</code> property.
      *
      * @parameter  expression="${cids.default.i18n.locale}"
      * @required   false
      * @readonly   true
      */
     private transient String defaultLocale;
 
     /**
      * The <code>default.i18n.inputDirectory</code> property.
      *
      * @parameter  expression="${cids.default.i18n.inputDirectory}"
      * @required   true
      */
     private transient File inputDirectory;
 
     /**
      * Plexus ArchiverManager.
      *
      * @component  DOCUMENT ME!
      */
     private transient ArchiverManager archiverManager;
 
     /**
      * Maven ProjectHelper.
      *
      * @component  DOCUMENT ME!
      */
     private transient MavenProjectHelper projectHelper;
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Executes the generate-i18n goal.
      *
      * @throws  MojoExecutionException  if any error occurs during mojo execution
      * @throws  MojoFailureException    if any error occurs during mojo execution
      */
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         if (skip) {
             if (getLog().isInfoEnabled()) {
                getLog().info("generate i18n artifacts skipped"); // NOI18N}
             }

             return;
         }
 
         final Log log = getLog();
 
         // first resolve all available resource files
         final File[] allResources = getAllFiles(inputDirectory, new RecursiveNBPropertiesFilter(), true);
         if (log.isInfoEnabled()) {
             log.info("found " + allResources.length + " resource files"); // NOI18N
         }
 
         // after that resolve all available locales
         final Set<Locale> availableLocales = scanLocales(allResources);
         if (log.isInfoEnabled()) {
             log.info("found " + availableLocales.size() + " different locales"); // NOI18N
             if (log.isDebugEnabled()) {
                 log.debug("available locales: " + availableLocales);             // NOI18N
             }
         }
 
         try {
             // generate a localised jar for every locale ...
             for (final Locale locale : availableLocales) {
                 final File jar = generateLocalisedJar(locale, allResources);
                 // ... and attach them to the build if anything was generated
                 if (jar != null) {
                     projectHelper.attachArtifact(project, jar, locale.toString());
                 }
             }
 
             // generate a localised jar for the default locale if the default locale is set ...
             if (defaultLocale != null) {
                 final File jar = generateLocalisedJar(null, allResources);
                 // ... and attach it to the build if anything was generated
                 if (jar != null) {
                     projectHelper.attachArtifact(project, jar, defaultLocale);
                 }
             }
         } catch (final ArchiverException ex) {
             throw new MojoExecutionException("error while adding entries to the archiver", ex); // NOI18N
         } catch (final IOException ex) {
             throw new MojoExecutionException("error while writing archive", ex);                // NOI18N
         }
     }
 
     /**
      * Generates a jar containing the localised properties. If no files were found within the given <code>File[]</code>
      * regarding the given locale no file will be created and null is returned.
      *
      * @param   locale           the desired <code>Locale</code> or null for default <code>Locale</code>
      * @param   propertiesFiles  the properties files that shall be filtered by locale
      *
      * @return  the output jar or <code>null</code> if no files were added to the archive
      *
      * @throws  ArchiverException         if an error occurs while adding an entry
      * @throws  IOException               if an error occurs while writing the archive
      * @throws  IllegalArgumentException  if the propertiesFiles argument is null
      */
     private File generateLocalisedJar(final Locale locale, final File... propertiesFiles) throws ArchiverException,
         IOException,
         IllegalArgumentException {
         if (propertiesFiles == null) {
             throw new IllegalArgumentException("Parameter propertyFiles is null");
         }
 
         // prepare the archiver
         final Archiver archiver;
         try {
             archiver = archiverManager.getArchiver(EXT_DEFAULT);                // NOI18N
         } catch (final NoSuchArchiverException ex) {
             throw new IllegalStateException("could not find jar archiver", ex); // NOI18N
         }
 
         // filter all localised files and add them to the archiver
         final RecursiveLocalisedNBPropertiesFilter filter = new RecursiveLocalisedNBPropertiesFilter(locale);
         int fileCount = 0;
         for (final File file : propertiesFiles) {
             if (filter.accept(file)) {
                 archiver.addFile(
                     file,
                     stripLocale(chRoot(inputDirectory.getAbsolutePath(), file.getAbsolutePath()), locale));
                 ++fileCount;
             }
         }
 
         // finally write the archive if files were added
         final File outFile;
         if (fileCount == 0) {
             if (getLog().isWarnEnabled()) {
                 getLog().warn("the given files do not contain any properties file with the given locale: " + locale); // NOI18N
             }
 
             outFile = null;
         } else {
             if (getLog().isDebugEnabled()) {
                 getLog().debug("added " + fileCount + " localised files"); // NOI18N
             }
 
             outFile = createOutputFile(locale);
             archiver.setDestFile(outFile);
             archiver.createArchive();
         }
 
         return outFile;
     }
 
     /**
      * Changes the root of a path what means that the resulting path will be relative to the given root. It additionally
      * converts backslashes to forward slashes. The resulting path won't have a leading slash.<br/>
      *
      * <ul>
      *   <li>If the given root is null then the path is returned with converted slashes.</li>
      *   <li>If the given root path is not a valid parent path of the given path the given path is returned with
      *     converted slashes</li>
      *   <li>If the given path is null an <code>IllegalArgumentException</code> will be thrown.</li>
      * </ul>
      *
      * @param   root  the new root
      * @param   path  the path to be chrooted
      *
      * @return  the chrooted path, never null
      *
      * @throws  IllegalArgumentException  if the given path is null
      */
     private String chRoot(final String root, final String path) {
         if (path == null) {
             throw new IllegalArgumentException("the given path must not be null");
         }
 
         final String chRootedPath;
 
         // strip the root path if it is not null
         if ((root != null) && path.startsWith(root)) {
             chRootedPath = path.substring(root.length(), path.length());
         } else {
             chRootedPath = path;
         }
 
         // convert '\\' to '/' and remove leading '/' if present
         String convertedPath = chRootedPath.replace('\\', '/');
         if ('/' == convertedPath.charAt(0)) {
             convertedPath = convertedPath.substring(1);
         }
 
         return convertedPath;
     }
 
     /**
      * Removes the given locale from the given string if none of the arguments is null and the path has localisation
      * identifiers as specified by the locale. In any other case the original path is returned.<br/>
      * This implementation assumes that:
      *
      * <ul>
      *   <li>the last dot within the path denotes the path's (file's) extension (e.g. MyRes.properties has the extension
      *     'properties') and the localisation identifiers will then be placed right before the dot (e.g.
      *     MyRes_de_DE_EUR.properties has language 'de', country 'DE' and variant 'EUR')</li>
      *   <li>if there is no dot in within the path (e.g. /tmp/MyRes) the very last characters of the path denote the
      *     localisation identifiers (e.g. MyRes_de_DE_EUR has language 'de', country 'DE' and variant 'EUR')</li>
      * </ul>
      *
      * @param   path    path containing a localised file name
      * @param   locale  the locale to be stripped
      *
      * @return  a <code>String</code> that does not contain the given locale anymore or the given path
      */
     private String stripLocale(final String path, final Locale locale) {
         if ((path == null) || (locale == null)) {
             return path;
         }
 
         final String stripped;
         final String localeString = locale.toString();
         if (localeString.length() >= path.length()) {
             // the path cannot have any localisation identifiers if it is shorter or equal the length of the
             // localisation identifiers
             stripped = path;
         } else if (path.lastIndexOf('.') == -1) {
             // if the path has no extension it is assumed that the localisation identifiers are at the end of the path
             final String tmp = path.substring(path.length() - localeString.length(), path.length());
             if (tmp.equals(localeString)) {
                 if ('_' == localeString.charAt(0)) {
                     stripped = path.replace(localeString, "");
                 } else {
                     stripped = path.replace("_" + localeString, "");
                 }
             } else {
                 // we could not identify the last characters as the given locale identifiers so we don't do anything
                 stripped = path;
             }
         } else {
             // the path as an extension so the chars before the '.' are considered the locale string and are removed
             // if the locale string exactly matches. Otherwise the path will not be changed.
             if ('_' == localeString.charAt(0)) {
                 stripped = path.replace(localeString + ".", ".");
             } else {
                 stripped = path.replace("_" + localeString + ".", ".");
             }
         }
 
         return stripped; // NOI18N
     }
 
     /**
      * Creates the output jar for the given <code>Locale</code>. The output file will be placed in the project's target
      * directory and will have the <code>Locale</code> as classifier. If the given <code>Locale</code> is null then the
      * default <code>Locale</code> will be taken as locale if it is set. If the default <code>Locale</code> is not set
      * either an <code>IllegalArgumentException</code> is thrown.
      *
      * @param   locale  the artifact's classifier
      *
      * @return  the output jar file
      *
      * @throws  IllegalArgumentException  if the given locale and the default locale are null
      */
     private File createOutputFile(final Locale locale) {
         final File targetDir = new File(project.getBuild().getDirectory());
         final String finalName = project.getBuild().getFinalName();
 
         // determine the locale in use
         final Locale currentLocale;
         if (locale == null) {
             if (defaultLocale == null) {
                 throw new IllegalArgumentException("given locale and defaultLocale are both null"); // NOI18N
             } else {
                 currentLocale = createLocale(defaultLocale.split("_"));                             // NOI18N
             }
         } else {
             currentLocale = locale;
         }
 
         return new File(targetDir, finalName + "-" + currentLocale + "." + EXT_DEFAULT); // NOI18N
     }
 
     /**
      * Scans for locales within a given array of properties files. The files are assumed to end with '.properties' and
      * possibly have a name containing up to three values that describe the {@link java.util.Locale} of that specific
      * file.<br/>
      * <b>NOTE: There is no check if all available localised files are complete with respect to the default localisation
      * or another available locale.</b>
      *
      * <p>TODO: probably move to a commons class/project</p>
      *
      * <p>TODO: this implementation does not support files with underscores in their names, fix if needed</p>
      *
      * @param   propertiesFiles  an array of properties files
      *
      * @return  a set that contains all available locales
      */
     private Set<Locale> scanLocales(final File... propertiesFiles) {
         final Log log = getLog();
         final HashSet<Locale> locales = new HashSet<Locale>();
 
         for (final File file : propertiesFiles) {
             final String filename = file.getName();
 
             // the files end with .properties so we search for the dot to substract the extension
             final int indexDot = filename.lastIndexOf('.');
             final String nameWOExtension;
             if (indexDot == -1) {
                 if (log.isWarnEnabled()) {
                     log.warn("cannot find file extension: " + filename); // NOI18N
                 }
                 nameWOExtension = filename;
             } else {
                 nameWOExtension = filename.substring(0, indexDot);
             }
             if (log.isDebugEnabled()) {
                 log.debug("filname w/o ext: " + nameWOExtension);
             }
 
             // as localised files look like "MyFile_de_DE_EUR" we search for the first '_' to substract the prefix
             final int indexBundle = filename.indexOf('_');                                             // NOI18N
             final String extractedLocale;
             if (indexBundle == -1) {
                 if (log.isDebugEnabled()) {
                     log.debug("file does not contain locale, assume default properties: " + filename); // NOI18N
                 }
                 // we set the string to "_" because the resulting array of the later split will the be of length 0
                 extractedLocale = "_";                             // NOI18N
             } else {
                 extractedLocale = nameWOExtension.substring(indexBundle + 1, nameWOExtension.length());
             }
             if (log.isDebugEnabled()) {
                 log.debug("extracted locale: " + extractedLocale); // NOI18N
             }
 
             // we create an instance of Locale from the extracted String ...
             final Locale locale = createLocale(extractedLocale.split("_"));
 
             // ... and add it to the available locales set if it is not null
             if (locale != null) {
                 locales.add(locale);
             }
         }
 
         return locales;
     }
 
     /**
      * Creates a locale from a splitted <code>String</code>.
      *
      * @param   splittedLocale  a splitted <code>String</code> array denoting a <code>Locale</code> (e.g. ["de", "DE"])
      *
      * @return  the <code>Locale</code> created from the <code>String</code> array or null if the length of the array is
      *          less than one and more than three (0 < length < 4)
      *
      * @throws  IllegalArgumentException  if given argument is null
      */
     private Locale createLocale(final String... splittedLocale) {
         if (splittedLocale == null) {
             throw new IllegalArgumentException("given argument is null"); // NOI18N
         }
 
         final String[] normalisedSplit = normaliseSplittedLocale(splittedLocale);
         switch (normalisedSplit.length) {
             case 0: {
                 // there is no locale so we don't do anything
                 return null;
             }
             case 1: {
                 // the locale contains only the language
                 return new Locale(normalisedSplit[0]);
             }
             case 2: {
                 // the locale contains language and country
                 return new Locale(normalisedSplit[0], normalisedSplit[1]);
             }
             case 3: {
                 // the locale contains language, country and variant
                 return new Locale(normalisedSplit[0], normalisedSplit[1], normalisedSplit[2]);
             }
             default: {
                 // all other cases are considered default and these indicate an illegal state
                 if (getLog().isWarnEnabled()) {
                     getLog().warn("invalid filepattern found: " + concat("_", normalisedSplit)); // NOI18N
                 }
                 return null;
             }
         }
     }
 
     /**
      * Normalises splitted locales by adding and '_' to the next token if the current token is empty and removes the
      * empty token.<br/>
      * <br/>
      * Example:<br/>
      *
      * <ul>
      *   <li>["", "de", "DE"] will result in ["_de", "DE"]</li>
      * </ul>
      *
      * @param   splittedLocale  the splitted locale to be normalised
      *
      * @return  the normalies splitted locale array
      *
      * @throws  IllegalArgumentException  if given argument is null
      */
     private String[] normaliseSplittedLocale(final String... splittedLocale) {
         if (splittedLocale == null) {
             throw new IllegalArgumentException("given argument is null"); // NOI18N
         }
 
         final ArrayList<String> normalised = new ArrayList<String>(splittedLocale.length);
 
         int i = 0;
         while (i < splittedLocale.length) {
             if (splittedLocale[i].isEmpty()) {
                 if ((i + 1) < splittedLocale.length) {
                     normalised.add("_" + splittedLocale[i + 1]);
                     // increment to skip next entry
                     ++i;
                 }
             } else {
                 normalised.add(splittedLocale[i]);
             }
             ++i;
         }
 
         return normalised.toArray(new String[normalised.size()]);
     }
 
     /**
      * Concatenates a <code>String</code> array using the given delimeter. If the delimeter is <code>null</code> the
      * values of the array will be concatenated without using a delimeter.
      *
      * <p>TODO: possibly move to a commons class/project</p>
      *
      * @param   delimeter       the delimeter between the parts
      * @param   splittedString  the <code>String</code> array to be concatenated.
      *
      * @return  the concatenated <code>String</code>
      *
      * @throws  IllegalArgumentException  if the splitted <code>String</code> array is <code>null</code>
      */
     private String concat(final String delimeter, final String... splittedString) {
         if (splittedString == null) {
             throw new IllegalArgumentException("splitted string may not be null"); // NOI18N
         }
         final String delim = (delimeter == null) ? "" : delimeter;
 
         final StringBuilder sb = new StringBuilder();
 
         // concat the parts using the given delim
         for (final String part : splittedString) {
             sb.append(part).append(delim);
         }
 
         // delete the last delimeter
         if ((splittedString.length >= delim.length()) && (delim != null)) {
             sb.delete(sb.length() - delim.length(), sb.length());
         }
 
         return sb.toString();
     }
 
     /**
      * Iterate through all files (descending into sub-directories) and list them in the return array if they are
      * accepted by the {@link java.io.FileFilter}. The return array will never be null nor will it ever contain
      * directories.
      *
      * <p>TODO: move to a commons class/project</p>
      *
      * @param   root        the directory from where the listing begins
      * @param   filefilter  an optional FileFilter, if <code>null</code> all files will be accepted
      * @param   recursive   whether to descend into sub-directories or not
      *
      * @return  an array listing all (accepted) files of the root directory and possibly all sub-directories
      *
      * @throws  IllegalArgumentException  DOCUMENT ME!
      */
     private File[] getAllFiles(final File root, final FileFilter filefilter, final boolean recursive) {
         if ((root == null) || !root.isDirectory()) {
             throw new IllegalArgumentException("cannot get files from normal file: " + root); // NOI18N
         }
 
         final ArrayList<File> files = new ArrayList<File>();
 
         // if there is no FileFilter we will accept all files
         final FileFilter filter = (filefilter == null) ? new NoFilter() : filefilter;
 
         for (final File file : root.listFiles(filter)) {
             if (file.isDirectory() && recursive) {
                 // recursively add all accepted sub-files
                 files.addAll(Arrays.asList(getAllFiles(file, filter, recursive)));
             } else if (file.isFile()) {
                 if (getLog().isDebugEnabled()) {
                     getLog().debug("adding file: " + file.getAbsolutePath()); // NOI18N
                 }
                 files.add(file);
             }
             // else skip the file because it is a directory and no recursion shall be done
         }
 
         return files.toArray(new File[files.size()]);
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * This is a FileFilter implementation that accepts properties files that start with "Bundle" and any directories.
      *
      * <p>TODO: move to a commons class/project</p>
      *
      * @version  1.0
      */
     private static final class RecursiveNBPropertiesFilter implements FileFilter {
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param   pathname  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         @Override
         public boolean accept(final File pathname) {
             return pathname.isDirectory()
                         || (pathname.getName().startsWith("Bundle") && pathname.getName().endsWith(".properties")); // NOI18N
         }
     }
 
     /**
      * This is a FileFilter implementation that accepts properties files that start with "Bundle" and contains the given
      * locale as well as any directories.
      *
      * <p>TODO: move to a commons class/project</p>
      *
      * @version  $Revision$, $Date$
      */
     private static final class RecursiveLocalisedNBPropertiesFilter implements FileFilter {
 
         //~ Instance fields ----------------------------------------------------
 
         private final transient Locale locale;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new RecursiveLocalisedNBPropertiesFilter object.
          *
          * @param  locale  the <code>Locale</code> by that the files shall be filtered. If the locale is null it will
          *                 accept the default properties.
          */
         public RecursiveLocalisedNBPropertiesFilter(final Locale locale) {
             this.locale = locale;
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param   pathname  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         @Override
         public boolean accept(final File pathname) {
             final String name = pathname.getName();
 
             if (pathname.isDirectory()) {
                 return true;
             } else if (locale == null) {
                 return "Bundle.properties".equals(name);                                                              // NOI18N
             } else {
                 return name.startsWith("Bundle") && name.endsWith(".properties") && name.contains(locale.toString()); // NOI18N
             }
         }
     }
 
     /**
      * This is a FileFilter implementation that has no restrictions on pathnames.
      *
      * <p>TODO: move to a commons class/project</p>
      *
      * @version  1.0
      */
     private static final class NoFilter implements FileFilter {
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param   pathname  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         @Override
         public boolean accept(final File pathname) {
             return true;
         }
     }
 }

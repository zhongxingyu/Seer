 package org.webreformatter.commons.uri;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author kotelnikov
  * @param <T>
  */
 public abstract class AbstractPathBuilder<T extends AbstractPathBuilder<?>>
     extends
     AbstractPath {
 
     public static class PathParseListener implements UriParser.IUriListener {
 
         private AbstractPathBuilder<?> fBuilder;
 
         public PathParseListener(AbstractPathBuilder<?> builder) {
             fBuilder = builder;
         }
 
         protected boolean doDecode() {
             return true;
         }
 
         public void onFragment(String fragment) {
         }
 
         public void onHost(String host) {
         }
 
         public void onPath(
             boolean absolute,
             List<String> segments,
             boolean hasTrailingSeparator) {
             fBuilder.fPathSegments = segments;
             if (doDecode()) {
                 int len = fBuilder.fPathSegments.size();
                 for (int i = 0; i < len; i++) {
                     String segment = fBuilder.fPathSegments.get(i);
                     segment = decode(segment);
                     fBuilder.fPathSegments.set(i, segment);
                 }
             }
             fBuilder.fPathAbsolute = absolute;
             fBuilder.fPathTrailingSeparator = hasTrailingSeparator;
         }
 
         public void onPort(int port) {
         }
 
         public void onQuery(String query) {
         }
 
         public void onScheme(List<String> segments) {
         }
 
         public void onUserInfo(String userInfo) {
         }
 
     }
 
     protected static void parsePath(
         String path,
         final AbstractPathBuilder<?> newEntity,
         final boolean decode,
         boolean resetExtraPathInfo) {
         newEntity.fPathSegments.clear();
         PathParseListener listener = newEntity.newParserListener();
         UriParser.parseFullPath(path, listener);
     }
 
     private static boolean resolvePath(List<String> segments) {
         boolean result = false;
         int len = segments.size();
         for (int i = 0; i < len; i++) {
             String segment = segments.get(i);
             if ("".equals(segment)
                 || ".".equals(segment)
                 || "..".equals(segment)) {
                 result = false;
                 segments.remove(i);
                 len--;
                 i--;
                 if ("..".equals(segment) && i >= 0) {
                     segments.remove(i);
                     len--;
                     i--;
                 }
             } else {
                 result = true;
             }
         }
         return result;
     }
 
     protected boolean fPathAbsolute;
 
     protected List<String> fPathSegments;
 
     protected boolean fPathTrailingSeparator;
 
     public AbstractPathBuilder() {
         this(null);
     }
 
     public AbstractPathBuilder(AbstractPath path) {
         setPath(path);
     }
 
     protected void addTrailingSeparator(boolean separator) {
         if (separator) {
             if (fPathSegments.size() == 0) {
                 fPathAbsolute = true;
                 fPathTrailingSeparator = false;
             } else {
                 fPathTrailingSeparator = true;
             }
         } else {
             fPathTrailingSeparator = false;
         }
     }
 
     /**
      * @param path the path to append to this one.
      * @return a new path containing all parts from this instance with appended
      *         segments from the given path
      */
     public T appendPath(AbstractPath path) {
         List<String> pathSegments = path.getPathSegments();
         if (path == null || (pathSegments.isEmpty() && !path.isAbsolutePath())) {
             return cast();
         }
         fPathSegments.addAll(pathSegments);
         fPathTrailingSeparator = path.hasPathTrailingSeparator();
         if (fPathSegments.isEmpty()) {
             fPathAbsolute = path.isAbsolutePath();
         }
         checkClonePath();
         return cast();
     }
 
     /**
      * Returns a copy of this path with appended path segments.
      * 
      * @param path the path segments to append to this path
      * @return a copy of this path with appended path segments.
      * @see #setPath(String)
      */
     public T appendPath(String path) {
         appendPath(path, false, DECODE_PATH);
         return cast();
     }
 
     /**
      * Returns a copy of this path with appended given segments.
      * 
      * @param path the path segments to append to this path
      * @return a copy of this path with appended path segments.
      * @see #setPath(String)
      */
     public T appendPath(String path, boolean begin) {
         return appendPath(path, begin, true);
     }
 
     /**
      * Returns a copy of this path with appended given segments.
      * 
      * @param segments the path segments to append to the path
      * @param begin if this flag is <code>true</code> then the specified
      *        segments will be added at the beginning of the path list;
      *        otherwise they will be appended at the end of the list
      * @return a copy of this path with appended segments.
      * @see #setPath(String)
      */
     public T appendPath(String segments, boolean begin, boolean decode) {
         if (segments == null) {
             return cast();
         }
         List<String> oldSegments = fPathSegments;
         boolean oldAbsolute = fPathAbsolute;
         fPathSegments = new ArrayList<String>();
         parsePath(segments, this, decode, false);
         if (begin) {
             fPathSegments.addAll(oldSegments);
        } else {
            if (!oldSegments.isEmpty()) {
                fPathSegments.addAll(0, oldSegments);
                fPathAbsolute = oldAbsolute;
            } else {
                fPathAbsolute |= oldAbsolute;
            }
         }
         checkClonePath();
         return cast();
     }
 
     protected void appendPath(StringBuffer buf, boolean escape, boolean encode) {
         appendPath(this, buf, escape, encode);
     }
 
     /**
      * Returns a copy of this path with appended or prepended path segments
      * taken from the given external path.
      * 
      * @param path the source of path segments to append to this path
      * @param begin if this flag is <code>true</code> then the specified
      *        segments will be added at the beginning of the path list;
      *        otherwise they will be appended at the end of the list
      * @return a copy of this path with appended path segments.
      * @see #setPath(String)
      */
     public T appendPathSegments(AbstractPath path, boolean begin) {
         int pos = begin ? 0 : fPathSegments.size();
         fPathSegments.addAll(pos, path.getPathSegments());
         checkClonePath();
         return cast();
     }
 
     /**
      * Returns a copy of this path with appended path segments.
      * 
      * @param begin if this flag is <code>true</code> then the specified
      *        segments will be added at the beginning of the path list;
      *        otherwise they will be appended at the end of the list
      * @param segments the path segments to append to this path
      * @return a copy of this path with appended path segments.
      * @see #setPath(String)
      */
     public T appendPathSegments(boolean begin, String... segments) {
         int pos = begin ? 0 : fPathSegments.size();
         for (String segment : segments) {
             fPathSegments.add(pos, segment);
             pos++;
         }
         checkClonePath();
         return cast();
     }
 
     /**
      * Returns a copy of this path with appended path segments.
      * 
      * @param segments the path segments to append to this path
      * @return a copy of this path with appended path segments.
      * @see #setPath(String)
      */
     public T appendPathSegments(String... segments) {
         return appendPathSegments(false, segments);
     }
 
     /**
      * Returns a copy of this path with appended segments.
      * 
      * @param segments the path segments to append to the path
      * @param begin if this flag is <code>true</code> then the specified
      *        segments will be added at the beginning of the path list;
      *        otherwise they will be appended at the end of the list
      * @return a copy of this path with appended given path segments.
      * @see #setPath(String)
      */
     public T appendPathSegments(String segments, boolean begin) {
         return appendPath(segments, begin, DECODE_PATH);
     }
 
     /**
      * @return an path with setted trailing separator
      */
     public T appendTrailingSeparator() {
         if (!fPathTrailingSeparator) {
             addTrailingSeparator(true);
         }
         return cast();
     }
 
     /**
      * @return
      */
     @SuppressWarnings("unchecked")
     protected T cast() {
         return (T) this;
     }
 
     protected void checkClonePath() {
         if (fPathSegments.isEmpty()) {
             if (!fPathAbsolute) {
                 fPathAbsolute = fPathTrailingSeparator;
             }
             fPathTrailingSeparator = false;
         }
     }
 
     public void clear() {
         fPathAbsolute = false;
         fPathSegments.clear();
         fPathTrailingSeparator = false;
     }
 
     public abstract T getCopy();
 
     /**
      * Returns the path without file name.
      * 
      * @param escapeSegments if this flag is <code>true</code> then this method
      *        will escape path segments
      * @return the path without file name
      */
     public T getDirectoryPath() {
         if (fPathTrailingSeparator) {
             return cast();
         }
         return removeLastPathSegments(1);
     }
 
     /**
      * Returns a new instance with the parent path.
      * 
      * @return a new instance of this type with the parent path
      */
     public T getParent() {
         if (fPathSegments.isEmpty()) {
             return cast();
         }
         int idx = fPathSegments.size() - 1;
         if (idx >= 0) {
             fPathSegments.remove(idx);
             fPathTrailingSeparator = (idx > 0);
         }
         checkClonePath();
         return cast();
     }
 
     @Override
     public String getPathSegment(int pos) {
         if (pos < 0 || pos >= fPathSegments.size()) {
             return null;
         }
         return fPathSegments.get(pos);
     }
 
     @Override
     public List<String> getPathSegments() {
         return fPathSegments;
     }
 
     /**
      * Resolves all relative path segments (like "." and "..") and returns the
      * absolute path.
      * 
      * @return the path with all resolved relative segments (like "." and "..")
      */
     public T getResolved() {
         resolvePath(fPathSegments);
         checkClonePath();
         return cast();
     }
 
     /**
      * This method resolves the given path relatively this one and returns the
      * resulting absolute path. If the given path is an absolute path ( it
      * contains a scheme, user info, a host or a port) then it will be returned
      * unchanged.
      * 
      * @param path a path to resolve
      * @return an absolute path resolved relatively to this one
      */
     public T getResolved(AbstractPath path) {
         if (path.isAbsolutePath()) {
             fPathSegments.clear();
             fPathSegments.addAll(path.getPathSegments());
             fPathAbsolute = true;
             fPathTrailingSeparator = path.hasPathTrailingSeparator();
             return cast();
         }
         List<String> pathSegments = path.getPathSegments();
         if (pathSegments.isEmpty()) {
             return cast();
         }
         if (!fPathSegments.isEmpty() && !fPathTrailingSeparator) {
             fPathSegments.remove(fPathSegments.size() - 1);
         }
         checkClonePath();
         fPathSegments.addAll(pathSegments);
         fPathTrailingSeparator = path.hasPathTrailingSeparator();
         boolean hasRealSegments = resolvePath(fPathSegments);
         if (hasRealSegments) {
             fPathTrailingSeparator = path.hasPathTrailingSeparator();
         } else {
             if (fPathSegments.isEmpty()) {
                 fPathTrailingSeparator = path.hasPathTrailingSeparator();
                 fPathAbsolute = false;
             } else {
                 fPathTrailingSeparator = true;
             }
         }
         checkClonePath();
         return cast();
     }
 
     @Override
     public boolean hasPathTrailingSeparator() {
         return fPathTrailingSeparator;
     }
 
     @Override
     public boolean isAbsolutePath() {
         return fPathAbsolute;
     }
 
     public boolean isEmpty() {
         return fPathSegments.isEmpty();
     }
 
     /**
      * @return a new path containing a path separator at the beginning
      */
     public T makeAbsolutePath() {
         fPathAbsolute = true;
         checkClonePath();
         return cast();
     }
 
     /**
      * @param path this method returns a path relative to this one
      * @return a path containing a relative path from this path to the given one
      */
     public T makeRelative(AbstractPath path) {
         boolean absolute = path.isAbsolutePath();
         if (fPathAbsolute && !absolute) {
             return cast();
         }
         if (!fPathAbsolute && absolute) {
             return setPath(path);
         }
         resolvePath(fPathSegments);
         List<String> pathSegments = new ArrayList<String>(
             path.getPathSegments());
         resolvePath(pathSegments);
 
         if (!fPathSegments.isEmpty() && !fPathTrailingSeparator) {
             fPathSegments.remove(fPathSegments.size() - 1);
             fPathTrailingSeparator = !fPathSegments.isEmpty();
         }
 
         int pathLen = pathSegments.size();
         if (pathLen > 0 && !path.hasPathTrailingSeparator()) {
             pathLen--;
         }
         int len = Math.min(fPathSegments.size(), pathLen);
         int start;
         for (start = 0; start < len; start++) {
             String a = fPathSegments.get(start);
             String b = pathSegments.get(start);
             if (!a.equals(b)) {
                 break;
             }
         }
         List<String> newPathSegments = new ArrayList<String>();
         for (int i = start; i < fPathSegments.size(); i++) {
             newPathSegments.add("..");
         }
         for (int i = start; i < pathSegments.size(); i++) {
             String b = pathSegments.get(i);
             newPathSegments.add(b);
         }
         fPathSegments = newPathSegments;
         // fPathTrailingSeparator = !fPathSegments.isEmpty()
         // && path.hasPathTrailingSeparator();
         if (fPathSegments.isEmpty()) {
             fPathSegments.add(".");
         }
         fPathTrailingSeparator = path.hasPathTrailingSeparator();
         fPathAbsolute = false;
         checkClonePath();
         return cast();
     }
 
     /**
      * Create (if it is possible) a new relative.
      * 
      * @return a new relative path
      */
     public T makeRelativePath() {
         fPathAbsolute = false;
         checkClonePath();
         return cast();
     }
 
     public PathParseListener newParserListener() {
         return new PathParseListener(this);
     }
 
     /**
      * Returns a copy of this path with removed first path segments
      * 
      * @param count the number of segments to remove
      * @return an path with removed first path segments
      */
     public T removeFirstPathSegments(int count) {
         if (!fPathSegments.isEmpty()) {
             count = Math.max(0, Math.min(count, fPathSegments.size()));
             for (int i = 0; i < count; i++) {
                 fPathSegments.remove(0);
             }
         }
         checkClonePath();
         return cast();
     }
 
     /**
      * Returns a copy of this path with removed last path segments
      * 
      * @param count the number of segments to remove
      * @return an path with removed last path segments
      */
     public T removeLastPathSegments(int count) {
         return removeLastPathSegments(count, false);
     }
 
     /**
      * Returns a copy of this path with removed last path segments. If the flag
      * <code>folderSegmentsOnly</code> is <code>true</code> then this method
      * will remove only folder segments and it keep the file name untouched.
      * 
      * <pre>
      * folderSegmentsOnly == true:
      *    "a/b/file.xml" => "a/file.xml" => "file.xml" => ""
      *    "a/b/c/"       => "a/b/"       => "a/"       => "" 
      *    
      * folderSegmentsOnly == false:
      *    "a/b/file.xml" => "a/b"  => "a"  => ""
      *    "a/b/c/"       => "a/b/" => "a/" => ""
      * </pre>
      * 
      * @param count the number of segments to remove
      * @param folderSegmentsOnly if this flag is <code>true</code> then this
      *        method will remove only the folder segments
      * @return an path with removed last path segments
      */
     public T removeLastPathSegments(int count, boolean folderSegmentsOnly) {
         count = Math.max(0, Math.min(count, fPathSegments.size()));
         if (count == 0) {
             return cast();
         }
         int len = fPathSegments.size();
         folderSegmentsOnly &= !fPathTrailingSeparator;
         if (folderSegmentsOnly) {
             len--;
         }
         for (int i = len - 1; i >= 0 && i >= len - count; i--) {
             fPathSegments.remove(i);
         }
         checkClonePath();
         return cast();
     }
 
     /**
      * @return a path with removed trailing separator
      */
     public T removeTrailingSeparator() {
         if (!fPathTrailingSeparator) {
             return cast();
         }
         addTrailingSeparator(false);
         checkClonePath();
         return cast();
     }
 
     /**
      * Returns a new path with the specified file extension. If this path has no
      * file name (it has a trailing separator) then this method do nothing and
      * just returns this path.
      * 
      * @param extension a new extension to set
      * @return a new path with the specified file extension
      */
     public T setFileExtension(String extension) {
         if (fPathTrailingSeparator) {
             return cast();
         }
         if (extension != null) {
             extension = extension.trim();
             if (extension.startsWith(".")) {
                 extension = extension.substring(1);
             }
         } else {
             extension = "";
         }
         extension = !"".equals(extension) ? "." + extension : "";
 
         String name = "";
         int len = fPathSegments.size();
         int pos = len;
         if (!fPathTrailingSeparator && len > 0) {
             String segment = fPathSegments.get(len - 1);
             int id = segment.lastIndexOf('.');
             name = (id >= 0) ? segment.substring(0, id) : segment;
             pos = len - 1;
             fPathSegments.set(pos, name + extension);
         } else {
             fPathSegments.add(name + extension);
         }
         fPathTrailingSeparator = false;
         return cast();
     }
 
     /**
      * Sets a new file name. If this path has a trailing separator then the
      * given file name will be appended to the path; otherwise the existing file
      * name is replaced by the given one.
      * 
      * @param fileName a file name to add to the path
      * @return a copy of this path with the specified file name
      */
     public T setFileName(String fileName) {
         int len = fPathSegments.size();
         if (!fPathTrailingSeparator && len > 0) {
             fPathSegments.remove(len - 1);
             if (len > 1) {
                 fPathTrailingSeparator = true;
             }
         }
         if (fileName != null) {
             fPathTrailingSeparator = false;
             fPathSegments.add(fileName);
         }
         return cast();
     }
 
     /**
      * Creates and returns a new path with the specified path, query and
      * fragment.
      * 
      * @param path the path containing (eventually) a query and an path fragment
      * @return a new path containing the specified path, query fragment
      * @see #setPath(String)
      */
     public T setFullPath(String path) {
         return setFullPath(path, DECODE_PATH);
     }
 
     /**
      * Creates and returns a new instance with the specified path, query and
      * fragment.
      * 
      * @param path the path containing (eventually) a query and an path fragment
      * @param decode if this flag is <code>true</code> then this method will
      *        decode all appended segments
      * @return a new instance containing the specified path, query fragment
      * @see #setPath(String)
      */
     public T setFullPath(String path, boolean decode) {
         fPathAbsolute = false;
         fPathSegments.clear();
         fPathTrailingSeparator = false;
         parsePath(path, this, decode, true);
         checkClonePath();
         return cast();
     }
 
     public T setPath(AbstractPath path) {
         if (path != null) {
             fPathAbsolute = path.isAbsolutePath();
             fPathTrailingSeparator = path.hasPathTrailingSeparator();
             fPathSegments = new ArrayList<String>(path.getPathSegments());
         } else {
             fPathSegments = new ArrayList<String>();
         }
         return cast();
     }
 
     /**
      * Creates and returns a new path with the specified path. This method keeps
      * the query and fragment parts.
      * 
      * @param path the path to set
      * @return a new instance with the specified path
      * @see #setFullPath(String)
      * @see #appendPath(String)
      * @see #appendPathSegments(String[])
      */
     public T setPath(String path) {
         return setPath(path, DECODE_PATH);
     }
 
     /**
      * Creates and returns a new instance with the specified path. This method
      * keeps the query and fragment parts.
      * 
      * @param path the path to set
      * @param decode if this flag is <code>true</code> then this method will
      *        decode all appended segments
      * @return a new instance with the specified path
      * @see #setFullPath(String)
      * @see #appendPath(String)
      * @see #appendPathSegments(String[])
      */
     public T setPath(String path, boolean decode) {
         fPathAbsolute = false;
         fPathTrailingSeparator = false;
         parsePath(path, this, decode, false);
         checkClonePath();
         return cast();
     }
 
 }

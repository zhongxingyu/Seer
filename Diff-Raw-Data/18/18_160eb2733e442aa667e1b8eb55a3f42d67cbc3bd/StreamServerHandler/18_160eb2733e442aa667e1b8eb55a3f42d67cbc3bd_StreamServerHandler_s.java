 /*
  * Copyright (c) 2011 Sergey Prilukin
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package jstreamserver.http;
 
 import anhttpserver.HttpRequestContext;
 import jstreamserver.dto.BreadCrumb;
 import jstreamserver.dto.FileListEntry;
 import jstreamserver.dto.Folder;
 import jstreamserver.utils.HttpUtils;
 import jstreamserver.utils.ffmpeg.FFMpegInformer;
 import jstreamserver.utils.ffmpeg.MediaInfo;
 import jstreamserver.utils.velocity.VelocityModel;
 import jstreamserver.utils.velocity.VelocityRenderer;
 import org.apache.commons.io.FilenameUtils;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * Main handler for the server.
  * All server logic put here.
  *
  * @author Sergey Prilukin
  */
 public final class StreamServerHandler extends BaseHandler {
 
     public static final String PATH_PARAM = "path";
     public static final String DIRECTORY_SEPARATOR = "/";
     public static final String ROOT_DIRECTORY = DIRECTORY_SEPARATOR;
     public static final String PARENT_FOLDER_NAME = "[..]";
 
     private ObjectMapper jsonMapper = new ObjectMapper();
    private FFMpegInformer ffMpegInformer = new FFMpegInformer();
 
     private static final Comparator<FileListEntry> FILE_LIST_COMPARATOR = new Comparator<FileListEntry>() {
         @Override
         public int compare(FileListEntry o1, FileListEntry o2) {
             if (PARENT_FOLDER_NAME.equals(o1.getName())) {
                 return -1;
             } else if (PARENT_FOLDER_NAME.equals(o2.getName())) {
                 return 1;
             } else if (o1.getDirectory() && !o2.getDirectory()) {
                 return -1;
             } else if (o2.getDirectory() && !o1.getDirectory()) {
                 return 1;
             } else {
                 return o1.getName().compareTo(o2.getName());
             }
         }
     };
 
     public StreamServerHandler() {
         super();
     }
 
     @Override
     public InputStream getResponseInternal(HttpRequestContext httpRequestContext) throws IOException {
         String path = HttpUtils.getURLParams(httpRequestContext.getRequestURI().getRawQuery()).get(PATH_PARAM);
         if (path != null) {
             path = URLDecoder.decode(path, HttpUtils.DEFAULT_ENCODING);
         }
 
         if (path == null || ROOT_DIRECTORY.equals(path)) {
             return renderDirectory(ROOT_DIRECTORY, getFilesFromNamesList(getConfig().getRootDirs().values()), httpRequestContext);
         }
 
         File file = getFile(path);
         if (file.exists() && file.isDirectory()) {
             return renderDirectory(path, getDirectoryContent(file), httpRequestContext);
         } else if (file.exists() && file.isFile()) {
             return getResource(file, httpRequestContext);
         } else {
             return rendeResourceNotFound(path, httpRequestContext);
         }
     }
 
     private List<File> getFilesFromNamesList(Collection<String> names) {
         List<File> files = new ArrayList<File>();
         for (String name: names) {
             files.add(new File(name));
         }
 
         return files;
     }
 
     private List<File> getDirectoryContent(File dir) {
         File[] files = dir.listFiles(new FileFilter() {
             @Override
             public boolean accept(File pathname) {
                 return !pathname.isHidden();
             }
         });
 
         return Arrays.asList(files);
     }
 
     private List<FileListEntry> getFiles(List<File> files, String parentPath) {
 
         List<FileListEntry> fileList = new ArrayList<FileListEntry>();
         List<FileListEntry> mediaFileList = new ArrayList<FileListEntry>();
         List<String> mediaFileNames = new ArrayList<String>();
 
         try {
 
             if (!ROOT_DIRECTORY.equals(parentPath)) {
                 String parentDirPath = parentPath.replaceAll("\\/$", "").replaceAll("\\/[^\\/]+$", "");
                 FileListEntry parentDir = new FileListEntry();
                 parentDir.setDirectory(true);
                 parentDir.setName(PARENT_FOLDER_NAME);
                 parentDir.setUrl(URLEncoder.encode(parentDirPath.isEmpty() ? ROOT_DIRECTORY : parentDirPath, HttpUtils.DEFAULT_ENCODING));
 
                 fileList.add(parentDir);
             }
 
             String parentDir = ROOT_DIRECTORY.equals(parentPath) ? ROOT_DIRECTORY : parentPath + DIRECTORY_SEPARATOR;
 
 
             for (File file : files) {
                 FileListEntry entry = new FileListEntry();
 
                 if (file.isFile()) {
                     String extension = FilenameUtils.getExtension(file.getName());
                     String mimeType = getMimeProperties().getProperty(extension.toLowerCase(), "application/octet-stream");
 
                     entry.setDirectory(false);
                     entry.setMimeType(mimeType);
                     entry.setExtension(extension);
                     entry.setLiveStreamSupported(getConfig().httpLiveStreamingSupported(extension, mimeType));
 
                     if (mimeType.startsWith("video") || mimeType.startsWith("audio")) {
                         mediaFileList.add(entry);
                         mediaFileNames.add(file.getPath());
                     }
                 } else {
                     entry.setDirectory(true);
                 }
 
                 String name = file.getName();
                 if (name.isEmpty()) {
                     name = file.getPath().replaceAll("[\\/\\\\\\:]", "");
                 }
 
                 entry.setName(name);
                 entry.setUrl(URLEncoder.encode(parentDir + name, HttpUtils.DEFAULT_ENCODING));
 
                 fileList.add(entry);
             }
 
             //Adding mediainfo to FileEnries
             if (!mediaFileNames.isEmpty()) {
                 List<MediaInfo> mediaInfos = ffMpegInformer.getInfo(mediaFileNames, getConfig().getFfmpegLocation());
                 for (int i = 0; i < mediaInfos.size(); i++) {
                     mediaFileList.get(i).setMediaInfo(mediaInfos.get(i));
                 }
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
 
         Collections.sort(fileList, FILE_LIST_COMPARATOR);
 
         Integer index = 0;
         for (FileListEntry file: fileList) {
             file.setId("fileList" + index++);
         }
 
         return fileList;
     }
 
     private List<BreadCrumb> generateBreadCrumbs(String path) {
         StringBuilder sb = new StringBuilder();
         List<BreadCrumb> breadCrumbs = new ArrayList<BreadCrumb>();
 
         String[] pathPieces = path.split("\\/");
         for (String dir: pathPieces) {
             if (sb.length() > 1 || sb.length() == 0) {
                 sb.append(DIRECTORY_SEPARATOR);
             }
 
             sb.append(dir);
             breadCrumbs.add(new BreadCrumb(dir.isEmpty() ? "HOME" : dir, sb.toString()));
         }
 
         return breadCrumbs;
     }
 
     private InputStream renderDirectory(String path, List<File> children, HttpRequestContext httpRequestContext) throws IOException {
         List<FileListEntry> files = getFiles(children, path);
         List<BreadCrumb> breadCrumbs = generateBreadCrumbs(path);
 
         InputStream result = null;
 
         if (isAjaxRequest(httpRequestContext)) {
             Folder folder = new Folder(files, breadCrumbs);
             byte[] filesJson = jsonMapper.writeValueAsBytes(folder);
             result = new ByteArrayInputStream(filesJson);
             setContentType("text/x-json", httpRequestContext);
         } else {
             VelocityModel model = new VelocityModel();
             model.put("files", jsonMapper.writeValueAsString(files));
             model.put("breadCrumbs", jsonMapper.writeValueAsString(breadCrumbs));
 
             result = VelocityRenderer.renderTemplate("templates/directory.vm", model);
             setContentType(HttpUtils.DEFAULT_TEXT_CONTENT_TYPE, httpRequestContext);
         }
 
         result = compressInputStream(result);
 
         setResponseHeader(HttpUtils.CONTENT_ENCODING_HEADER, HttpUtils.GZIP_ENCODING, httpRequestContext);
         setResponseCode(HttpURLConnection.HTTP_OK, httpRequestContext);
 
         return result;
     }
 }

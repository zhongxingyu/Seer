 /*
  * Copyright (c) 2011 Davis Marques
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301, USA.
  */
 package ryerson.daspub.mobile;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.exception.ExceptionUtils;
 import org.apache.sanselan.ImageReadException;
 import org.jpedal.exception.PdfException;
 import ryerson.daspub.Config;
 import ryerson.daspub.Config.SUBMISSION_EVALUATION;
 import ryerson.daspub.Config.SUBMISSION_TYPE;
 import ryerson.daspub.model.Assignment;
 import ryerson.daspub.model.Submission;
 import ryerson.daspub.utility.ImageUtils;
 import ryerson.daspub.utility.PDFUtils;
 import ryerson.daspub.utility.VideoUtils;
 
 /**
  * Assignment page.
  * @author dmarques
  */
 public class AssignmentPage {
 
     private static String FULL_DIR = "full";
     private static String THUMB_DIR = "thumb";
     private static String VIDEO_DIR = "video";
 
     private static String HANDOUT_FILENAME = "handout.jpg";
     private static String HANDOUT_THUMB_FILENAME = "handout-thumb.jpg";
 
     private static final Logger logger = Logger.getLogger(AssignmentPage.class.getName());
 
     //--------------------------------------------------------------------------
 
     /**
      * HTML thumbnail index of document Submissions matching the evaluation
      * value E.
      * @param A Assignment
      * @param E Evaluation value
      * @param Id Block identifier
      * @param Title Block title
      * @param Output Assignment output folder
      * @return PhotoSwipe gallery index
      */
     public static String buildDocumentSubmissionIndex(Assignment A, SUBMISSION_EVALUATION E, String Id, String Title, File Output) {
         List<Submission> ls = A.getSubmissions(SUBMISSION_TYPE.IMAGE, E);
         StringBuilder sb = new StringBuilder();
         if (ls.size() > 0) {
             // javascript for gallery function
             sb.append("\n\t<script type=\"text/javascript\">");
             sb.append("\n\t\t$(document).ready(function(){");
             sb.append("\n\t\tvar ");
             sb.append(Id);
             sb.append("Swipe = $(\"#");
             sb.append(Id);
             sb.append(" a\").photoSwipe({ enableMouseWheel: false , enableKeyboard: false });");
             sb.append("\n\t\t\t});");
             sb.append("\n\t</script>");
             // submission index
             sb.append("\n\t<div data-role=\"collapsible\" data-collapsed=\"true\">");
             sb.append("\n\t\t<h3>");
             sb.append(Title);
             sb.append("</h3>");
             sb.append("\n\t<ul id=\"");
             sb.append(Id);
             sb.append("\" class=\"gallery\">");
             // create output directories
             File full = new File(Output,FULL_DIR);
             if (!full.exists()) {
                 full.mkdirs();
             }
             File thumb = new File(Output,THUMB_DIR);
             if (!thumb.exists()) {
                 thumb.mkdirs();
             }
             // for each submission, add an index item
             Iterator<Submission> its = ls.iterator();
             List<File> images;
             List<File> thumbs;
             while (its.hasNext()) {
                 Submission s = its.next();
                 if (s.getSourceFile().exists()) {
                     // write thumbnails, full size images
                     images = writeImage(s,thumb,Config.THUMB_MAX_WIDTH,Config.THUMB_MAX_HEIGHT);
                     thumbs = writeImage(s,full,Config.IMAGE_MAX_WIDTH,Config.IMAGE_MAX_HEIGHT);
                     // build html
                     for (int i = 0; i < images.size(); i++) {
                         File f = images.get(i);
                         File t = thumbs.get(i);
                         sb.append("\n\t\t<li>");
                         sb.append("<a href=\"");
                         sb.append(A.getURLSafeName());
                         sb.append("/");
                         sb.append(FULL_DIR);
                         sb.append("/");
                         sb.append(f.getName());
                         sb.append("\" rel=\"external\"><img src=\"");
                         sb.append(A.getURLSafeName());
                         sb.append("/");
                         sb.append(THUMB_DIR);
                         sb.append("/");
                         sb.append(t.getName());
                         sb.append("\" alt=\"");
                         sb.append(s.getAssignmentName());
                         sb.append(", ");
                         sb.append(s.getAuthor());
                         if (s.isMultiPagePDF()) {
                             sb.append(getPageNumberLabel(s,f.getName()));                            
                         }
                         sb.append(" (");
                         sb.append(s.getEvaluationString());
                         sb.append(")\" /></a></li>");
                     }
                 }
             }
             sb.append("\n\t</ul>\n</div>");
         }
         // return result
         return sb.toString();
     }
 
     /**
      * HTML thumbnail index of course handout
      * @param A Assignment
      * @param Output Assignment output folder
      * @return PhotoSwipe gallery index
      */
     public static String buildSyllabusIndex(Assignment A, File Output) {
         StringBuilder sb = new StringBuilder();
         if (A.hasSyllabusFile()) {
             // javascript for gallery function
             sb.append("\n<script type=\"text/javascript\">");
             sb.append("\n\t$(document).ready(function(){");
             sb.append("\n\t\tvar sPhotoswipe = $(\"#syllabus a\").photoSwipe({enableMouseWheel:false,enableKeyboard:false});");
             sb.append("\n\t\t});");
             sb.append("\n</script>");
             // syllabus index
             File thumb = new File(Output,HANDOUT_THUMB_FILENAME);
             File full = new File(Output,HANDOUT_FILENAME);
             try {
                 List<File> thumbs;
                 List<File> images;
                 thumbs = PDFUtils.writeJPGImageAllPDFPages(A.getSyllabusFile(),
                                                         thumb,
                                                         Config.THUMB_MAX_WIDTH,
                                                         Config.THUMB_MAX_HEIGHT);
                 images = PDFUtils.writeJPGImageAllPDFPages(A.getSyllabusFile(),
                                                         full,
                                                         Config.IMAGE_MAX_WIDTH,
                                                         Config.IMAGE_MAX_HEIGHT);
                 // for each page of the syllabus, write out an index thumbnail
                 String url = A.getURLSafeName();
                 sb.append("\n<ul id=\"syllabus\" class=\"gallery\">");
                 for (int i = 0; i < images.size(); i++) {
                     File f = images.get(i);
                     File t = thumbs.get(i);
                     sb.append("\n\t<li>");
                     sb.append("<a href=\"");
                     sb.append(url);
                     sb.append("/");
                     sb.append(f.getName());
                     sb.append("\" rel=\"external\"><img src=\"");
                     sb.append(url);
                     sb.append("/");
                     sb.append(t.getName());
                     sb.append("\" alt=\"");
                     sb.append("Page ");
                     sb.append(i + 1);
                     sb.append(" of ");
                     sb.append(images.size());
                     sb.append("\" /></a></li>");
                 }
                 sb.append("\n</ul>");
             } catch (PdfException | IOException ex) {
                 String stack = ExceptionUtils.getStackTrace(ex);
                 logger.log(Level.SEVERE, "Could not write handout gallery for {0}\n\n{1}",
                         new Object[]{A.getFolder().getAbsolutePath(), stack});
             }
         }
         // return result
         return sb.toString();
     }
 
     /**
      * HTML thumbnail index of video submissions matching the evaluation value
      * E.
      * @param A Assignment
      * @param Evaluation Evaluation value
      * @param Id Block identifier
      * @param Title Block title
      * @param Output Assignment output folder
      * @return Video gallery index
      */
     public static String buildVideoSubmissionIndex(Assignment A,
                                                    SUBMISSION_EVALUATION Evaluation,
                                                    String Id,
                                                    String Title,
                                                    File Output)
     {
         StringBuilder sb = new StringBuilder();
         List<Submission> ls = A.getSubmissions(SUBMISSION_TYPE.VIDEO, Evaluation);
         if (ls.size() > 0) {
             // submission index
             sb.append("\n<div data-role=\"collapsible\" data-collapsed=\"true\">");
             sb.append("\n\t<h3>");
             sb.append(Title);
             sb.append("</h3>");
             sb.append("\n\t<ul id=\"");
             sb.append(Id);
             sb.append("\" class=\"gallery\">");
             Iterator<Submission> its = ls.iterator();
             // for each video, create a player and a poster image, copy the
             // source file to the output folder
             while (its.hasNext()) {
                 Submission s = its.next();
                 File full = new File(Output,HANDOUT_FILENAME);
                 File thumb = new File(Output,HANDOUT_THUMB_FILENAME);
                 if (s.getSourceFile().exists()) {
                     // write poster for video
                     //VideoUtils.writeJPGImage(null, null, Config.VIDEO_WIDTH, Config.VIDEO_HEIGHT);
                     // copy the source file to the output folder
                     sb.append("\n\t\t<li>");
                     sb.append("<a href=\"");
                     sb.append(A.getURLSafeName());
                     sb.append("/");
                     sb.append(VIDEO_DIR);
                     sb.append("/");
                     sb.append("video poster file name");
                     sb.append("\" rel=\"external\"><img src=\"");
                     sb.append(A.getURLSafeName());
                     sb.append("/");
                     sb.append(VIDEO_DIR);
                     sb.append("/");
                     sb.append(s.getSourceFileName());
                     sb.append("\" alt=\"");
                     sb.append(s.getAuthor());
                     sb.append("\" /></a></li>");
                 }
             }
             sb.append("\n\t</ul>");
             sb.append("\n</div>");
         }
         // return result
         return sb.toString();
     }
 
     /**
      * Create a page number label from the submission object and file name.
      * @return
      */
     private static String getPageNumberLabel(Submission S, String Name) {
         // get page number of file
         int i = Name.indexOf("-");
         int j = Name.lastIndexOf(".");
         CharSequence val = Name.subSequence(i+1,j);
         Integer page = Integer.valueOf(val.toString()); 
         page += 1; // pages are enumerated on a zero based index
         // build label
         int total = S.getPageCount();
         StringBuilder sb = new StringBuilder();
        sb.append(". Page ");
         sb.append(String.valueOf(page));
         sb.append(" of ");
         sb.append(String.valueOf(total));
         return sb.toString();
     }
 
     /**
      * Get thumbnail file name.
      * @param S Submission
      * @return
      */
     public static String getThumbnailFileName(Submission S) {
         if (S.getSourceFile().exists()) {
             return ImageUtils.getJPGFileName(S.getSourceFile().getName());
         }
         return "";
     }
 
     /**
      * Write assignment page.
      * @param A Assignment
      * @param Output Output folder
      */
     public static void Write(Assignment A, File Output) {
         logger.log(Level.INFO, "Writing assignment folder {0}", A.getFolder().getAbsolutePath());
         // create the output folder
         Output.mkdirs();
         try {
             // load template
             String page = FileUtils.readFileToString(new File(Config.ASSIGNMENT_TEMPLATE_PATH));
             // replace template variable fields
             page = page.replace("${title}", A.getName());
             page = page.replace("${description}", A.getDescription());
             // build index for syllabus
             if (A.hasSyllabusFile()) {
                 page = page.replace("${syllabus}", buildSyllabusIndex(A, Output));
             } else {
                 page = page.replace("${syllabus}", "\n<p>Assignment handout not available.</p>");
             }
             // build indicies for high pass and low pass document submissions
             if (A.hasImageSubmissions()) {
                 String index = "\n<div data-role=\"collapsible-set\">";
                 index += buildDocumentSubmissionIndex(A,
                                 SUBMISSION_EVALUATION.HIGH_PASS,
                                 "imagesHighPass",
                                 "Documents - High Pass",
                                 Output);
                 index += buildDocumentSubmissionIndex(A,
                                 SUBMISSION_EVALUATION.LOW_PASS,
                                 "imagesLowPass",
                                 "Documents - Low Pass",
                                 Output);
                 index += "\n</div>";
                 page = page.replace("${images}", index);
             } else {
                 page = page.replace("${images}", "");
             }
             // build indicies for high pass and low pass video submissions
             if (A.hasVideoSubmission()) {
                 String index = "\n<div data-role=\"collapsible-set\">";
                 index += buildVideoSubmissionIndex(A,
                                 SUBMISSION_EVALUATION.HIGH_PASS,
                                 "videoHighPass",
                                 "Animations and Videos - High Pass",
                                 Output);
                 index += buildVideoSubmissionIndex(A,
                                 SUBMISSION_EVALUATION.LOW_PASS,
                                 "videoLowPass",
                                 "Animations and Videos - Low Pass",
                                 Output);
                 index += "\n</div>";
                 page = page.replace("${video}",index);
             } else {
                 page = page.replace("${video}","");
             }
             // write assignment html page
             File html = new File(Output,"index.html");
             FileUtils.write(html, page);
         } catch (Exception ex) {
             String stack = ExceptionUtils.getStackTrace(ex);
             logger.log(Level.SEVERE,
                        "Could not write assignment {0} to {1}\n\n{2}",
                        new Object[]{A.getName(), Output, stack});
         }
     }
 
     /**
      * Write JPG thumbnail image of submission source file. If the source file
      * is a multi-page PDF, write all pages.
      * @param S Submission
      * @param Output Output folder
      * @param Width Maximum image width
      * @param Height Maximum image height
      * @return List of output files.
      */
     private static List<File> writeImage(Submission S, File Output, int Width, int Height) {
         // if output is a directory
         File output = Output;
         if (output.isDirectory()) {
             output = new File(output,S.getSourceFileName());
         }
         // create parent directory if it does not exist
         if (!output.getParentFile().exists()) {
             output.getParentFile().mkdirs();
         }
         // write output file(s)
         File input = S.getSourceFile();
         List<File> files = new ArrayList<>();
         try {
             if (S.isMultiPagePDF()) {
                 files = PDFUtils.writeJPGImageAllPDFPages(input,output,Width,Height);
             } else if (S.isSinglePagePDF()) {
                 files = PDFUtils.writeJPGImage(input,output,Width,Height);
             } else if (S.isImage()) {
                 ImageUtils.writeJPGImage(input,output,Width,Height);
                 files.add(output);
             } else if (S.isVideo()) {
                 VideoUtils.writeJPGImage(input,output,Width,Height);
                 files.add(output);
             }
         } catch (IOException | PdfException | ImageReadException ex) {
             String stack = ExceptionUtils.getStackTrace(ex);
             logger.log(Level.SEVERE,
                     "Could not write thumbnail {0} of {1}\n\n{2}",
                     new Object[]{output.getAbsolutePath(),
                                  input.getAbsolutePath(),
                                  stack});
         }
         // return list of output files
         return files;
     }
 
 } // end class

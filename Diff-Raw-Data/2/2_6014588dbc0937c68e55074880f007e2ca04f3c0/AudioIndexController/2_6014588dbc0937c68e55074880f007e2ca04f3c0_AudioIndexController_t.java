 /*
  * =============================================================================
  * 
  *   Copyright (c) 2011, The THYMELEAF team (http://www.thymeleaf.org)
  * 
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  * 
  * =============================================================================
  */
 package com.company.annotation.audio.web.controller;
 
 import com.company.annotation.audio.pojos.Comment;
 import com.company.annotation.audio.pojos.IndexSummary;
 import com.company.annotation.audio.services.IAnnotationService;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.*;
 
 @Controller
 public class AudioIndexController extends DefaultSpringController {
     private static Logger logger = Logger.getLogger( "com.company.annotation.audio" );
 
     @Autowired
     private IAnnotationService audioAnnotationService;
 
     @ModelAttribute("indexFiles")
     public List<IndexSummary> allIndexFiles() {
         return Arrays.asList(this.audioAnnotationService.loadAll());
     }
 
     @RequestMapping( value = "annotations", method = RequestMethod.GET )
     @Transactional
     public ModelAndView getAnnotations( @RequestParam String idIndexSummary ) {
         final IndexSummary indexSummary = audioAnnotationService.loadIndexSummary( idIndexSummary );
         final List<Comment> comments    = indexSummary.getComments();
 
         //TODO:  Fix lazy loading so that this works..
         // Sort based on how many comments are in a thread
 //        Collections.sort(comments, new Comparator<Comment>() {
 //            public int compare(Comment commentGraph2, Comment commentGraph1) {
 //                if ( commentGraph1.size() == commentGraph2.size() ) {
 //                    return commentGraph1.getDate().compareTo(commentGraph1.getDate());
 //                }
 //
 //                return ((Integer) commentGraph1.size() ).compareTo( commentGraph2.size() );
 //            }
 //        });
 
         Map modelMap = new HashMap();
         modelMap.put( "annotations", comments );
 
         return new ModelAndView( "annotations", modelMap );
     }
 
     @RequestMapping( value = "/", method = RequestMethod.GET )
     public ModelAndView showIndexFiles( ) {
         return new ModelAndView( "main" );
     }
 
     @RequestMapping( value = "annotations/add", method = RequestMethod.POST )
     @Transactional
     public @ResponseStatus( value = HttpStatus.NO_CONTENT ) void addAnnotation(
             @RequestParam String idIndexFile,
             @RequestParam String text,
             @RequestParam int startX,
             @RequestParam int endX) {
 
         logger.info("**** adding comment for file=" + idIndexFile + " region(" + startX + "," + endX + ") with text=" + text);
 
         final IndexSummary indexSummary = audioAnnotationService.loadIndexSummary( idIndexFile );
        indexSummary.getComments().add( new Comment( text, Math.min( startX, endX ), Math.max( startX, endX ) ) );
 
         audioAnnotationService.save( indexSummary );
     }
 }

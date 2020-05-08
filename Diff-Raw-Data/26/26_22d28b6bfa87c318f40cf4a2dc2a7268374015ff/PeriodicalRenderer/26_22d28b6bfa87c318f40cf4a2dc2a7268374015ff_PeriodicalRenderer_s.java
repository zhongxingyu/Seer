 /*
  * Created on Oct 24, 2003
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package net.cyklotron.cms.periodicals;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.Map;
 
 import javax.mail.MessagingException;
 
 import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.Resource;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.MergingException;
 import org.objectledge.templating.TemplateNotFoundException;
 
 import net.cyklotron.cms.category.query.CategoryQueryResource;
 import net.cyklotron.cms.files.FileResource;
 
 /**
  * An utility class for rendering periodicals.
  * 
  * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
  * @version $Id: PeriodicalRenderer.java,v 1.11 2006-05-09 10:38:38 rafal Exp $ 
  */
 public interface PeriodicalRenderer
 {
     /**
      * Return the renderer name.
      * 
      * @return the renderer name.
      */
     public String getName();
     
     /**
      * Prepares the renderer for rendering a periodical.
      * @param coralSession the coral session.
      * @param periodical the periodical.
      * @param time publication time.
      * @param template name name of the template variant to use.
      * @param file target file
      * @param contentFile previously rendered server side content file for notification renderers,
      * null otherwise.
      * 
      * @return <code>true</code> on success.
      * @throws PeriodicalsException 
      * @throws TemplateNotFoundException 
      * @throws MergingException 
      * @throws ProcessingException 
      * @throws MessagingException 
      * @throws IOException 
      */
     public void render(CoralSession coralSession, PeriodicalResource periodical,
        Map<CategoryQueryResource, Resource> queryResults, Date time, String templateName,
         FileResource file, FileResource contentFile)
         throws ProcessingException, MergingException, TemplateNotFoundException,
         PeriodicalsException, IOException, MessagingException;
     
     /**
      * Returns the suffix of the filename of the generated periodical.
      * 
      * @return the suffix.
      */
     public String getFilenameSuffix();
     
     /**
      * Returns the content type of the generated file.
      *
      * @return the content type of the generated file.
      */
     public String getMimeType();    
 }

 package fr.ultimate.breakfast.web.services.exceptionHandler;
 
 import java.io.IOException;
 
 import org.apache.tapestry5.services.ComponentSource;
 import org.apache.tapestry5.services.ExceptionReporter;
 import org.apache.tapestry5.services.RequestExceptionHandler;
 import org.apache.tapestry5.services.RequestGlobals;
 import org.apache.tapestry5.services.ResponseRenderer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.ultimate.breakfast.common.exception.BusinessException;
 import fr.ultimate.breakfast.common.exception.TechnicalException;
 
 /**
  * Application Exception Handler
  * React for {@link TechnicalException}, {@link BusinessException} and other {@link Exception}
  * 
  * @author jmaupoux
  */
 public class UltimateBreakfastRequestExceptionHandler implements RequestExceptionHandler
 {
     /**
      * Logger
      */
     private static final Logger LOGGER = LoggerFactory.getLogger(UltimateBreakfastRequestExceptionHandler.class);
 
     /**
      * Service de gestion des exceptions
      */
     private final ExceptionHandlerService exceptionService;
 
     /**
      * Service d'acces au composant
      */
     private final ComponentSource componentSource;
 
     /**
      * Response renderer
      */
     private final ResponseRenderer renderer;
 
     /**
      * Global request
      */
     private final RequestGlobals request;
 
     /**
      * Production Mode
      */
     private boolean productionMode;
 
     /**
      * Page Tapestry d'exception par defaut
      */
     private static final String STANDARD_T5_EXCEPTION_REPORT_PAGE = "ExceptionReport";
 
     /**
      * Constructeur
      * 
      * @param exceptionService exceptionService
      * @param componentSource componentSource
      * @param renderer renderer
      * @param request request
      * @param productionMode productionMode
      */
     public UltimateBreakfastRequestExceptionHandler(ExceptionHandlerService exceptionService, ComponentSource componentSource, ResponseRenderer renderer,
             RequestGlobals request, boolean productionMode)
     {
         this.exceptionService = exceptionService;
         this.componentSource = componentSource;
         this.renderer = renderer;
         this.request = request;
        this.productionMode = productionMode;
     }
 
     /**
      * Gestion d'une exception
      * 
      * @param exception l'exception à traiter
      * @throws IOException en cas de pb lors du retour du renderer
      */
     public void handleRequestException(Throwable exception) throws IOException
     {
         String pageName = "";
 
         BusinessException b = exceptionService.getNestedExceptionOfType(exception, BusinessException.class);
 
         if (b != null)
         {
             request.getRequest().setAttribute(ExceptionHandlerService.REQ_BUSINESS_MESSAGE, b.getMessage());
             pageName = componentSource.getActivePage().getComponentResources().getPageName();
         }
         else
         {
             if (productionMode)
             {
                 LOGGER.error("Unexpected exception: " + exception.getMessage(), exception);
                 exception.printStackTrace();
                 ExceptionReporter error = (ExceptionReporter) componentSource.getPage(ExceptionHandlerService.ERROR_PAGE);
                 error.reportException(exception);
                 pageName = ExceptionHandlerService.ERROR_PAGE;
             }
             else
             {
                 ExceptionReporter error = (ExceptionReporter) componentSource.getPage(STANDARD_T5_EXCEPTION_REPORT_PAGE);
                 error.reportException(exception);
                 pageName = STANDARD_T5_EXCEPTION_REPORT_PAGE;
             }
         }
         renderer.renderPageMarkupResponse(pageName);
     }
 }

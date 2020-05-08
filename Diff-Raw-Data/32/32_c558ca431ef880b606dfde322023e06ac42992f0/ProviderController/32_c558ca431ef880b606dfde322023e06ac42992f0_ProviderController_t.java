 /*******************************************************************************
  * Copyright (c) 2013 lmatas.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  *     Lautaro Matas (lmatas@gmail.com) - Desarrollo e implementación
  *     Emiliano Marmonti(emarmonti@gmail.com) - Coordinación del componente III
  * 
  * Este software fue desarrollado en el marco de la consultoría "Desarrollo e implementación de las soluciones - Prueba piloto del Componente III -Desarrollador para las herramientas de back-end" del proyecto “Estrategia Regional y Marco de Interoperabilidad y Gestión para una Red Federada Latinoamericana de Repositorios Institucionales de Documentación Científica” financiado por Banco Interamericano de Desarrollo (BID) y ejecutado por la Cooperación Latino Americana de Redes Avanzadas, CLARA.
  ******************************************************************************/
 package org.lareferencia.provider.controller;
 
 import java.io.IOException;
import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
 import org.lareferencia.backend.stats.ProviderStatsManager;
 import org.lareferencia.provider.domain.MetadataFormat;
 import org.lareferencia.provider.domain.Record;
 import org.lareferencia.provider.exceptions.BadArgumentException;
 import org.lareferencia.provider.exceptions.CannotDisseminateFormatException;
 import org.lareferencia.provider.exceptions.NoMetadataFormatsException;
 import org.lareferencia.provider.exceptions.ProtocolException;
 import org.lareferencia.provider.exceptions.ServerException;
 import org.lareferencia.provider.providers.IProvider;
 import org.lareferencia.provider.providers.StateHolder;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.propertyeditors.StringTrimmerEditor;
 import org.springframework.context.MessageSource;
 import org.springframework.context.MessageSourceAware;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 
 public class ProviderController extends MultiActionController implements MessageSourceAware
 {
    //private final Log log = LogFactory.getLog(getClass());
 	
    @Autowired	
    private ProviderStatsManager statsManager;	 
   
   @Autowired
   @Qualifier("adminEmail")
   private String adminEmail;	 
 
    private MessageSource messages = null;
    private IProvider provider = null;
    private Map<String, MetadataFormat> metadataFormatMap = new HashMap<String, MetadataFormat>();
   
   private static SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

 
    public ProviderController()
    {
       super();
    }
 
 	/* Accessors/Mutators
 	 **************************************************************************/
 
 	public void setMessageSource(final MessageSource messages)
 	{
 		this.messages = messages;
 	}
 
 	public MessageSource getMessageSource()
 	{
 		return this.messages;
 	}
    
 	public void setProvider(final IProvider provider)
 	{
 		this.provider = provider;
 	}
 
 	public IProvider getProvider()
 	{
 		return this.provider;
 	}
    
    public List<MetadataFormat> getMetadataFormats() throws NoMetadataFormatsException, ProtocolException
    {
       final List<MetadataFormat> formats = new ArrayList<MetadataFormat>();
       formats.addAll(this.metadataFormatMap.values());
       return formats;
    }
 
    public void setMetadataFormats(final List<MetadataFormat> formats)
    {
       if(formats == null)
          throw new IllegalArgumentException("'setMetadataFormats()' expects a non-null Collection of MetadataFormat instances.");
 
       this.metadataFormatMap.clear();
       
       for(MetadataFormat format : formats)
          this.metadataFormatMap.put(format.getMetadataPrefix(), format);
    }
 
 	/* MultiActionController Methods
 	 **************************************************************************/
 
 	@Override
    protected void initBinder(final HttpServletRequest request, final ServletRequestDataBinder binder)
    {
       binder.registerCustomEditor(String.class, null, new StringTrimmerEditor(true));
    }
 
 	public ModelAndView defaultHandler(final HttpServletRequest request, final HttpServletResponse response, final Arguments arguments)
 	{
	  final Map<String, Object> model = new HashMap<String, Object>();
 
       // Debugging.
       response.setContentType("text/xml; charset=UTF-8");
       
       // registro de la ip para estadísticas
       statsManager.countIPAccess( request.getRemoteAddr() );
 
       // The OAI protocol includes the original HTTP request.
	  final String requestURL = request.getRequestURL().toString();
	  model.put("RequestURL", requestURL);
		
	 // responseDate
	  String dateString = dateFormater.format( new DateTime().toDate() );
	  System.out.println(dateString);
	  model.put("ResponseDate", dateString);
	  
	 // adminEmail
	  model.put("AdminEmail", adminEmail);
	  
       try
       {
          if(this.getProvider() == null)
             throw new ServerException("There was a configuration problem. ProviderController expects a non-null 'provider'.");
             
          // The 'verb' parameter must be set.
          if(arguments.getVerb() == null)
             throw new BadArgumentException("A 'verb' argument is required.");
          
          // Restrict arguments just to the ones defined by the spec.
          final String[] allowedArguments = { "from", "identifier", "metadataPrefix",
             "resumptionToken", "set", "until", "verb" };
 
          final Set<String> allowedArgumentSet = new HashSet<String>();
          for(String argument : allowedArguments)
             allowedArgumentSet.add(argument);
 
          Set<String> requestParameters = (Set<String>)request.getParameterMap().keySet();
          for(String parameter : requestParameters)
          {
             if(!allowedArgumentSet.contains(parameter))
                throw new BadArgumentException("Unknown argument '" + parameter + "'.");
          }
 
          IProvider provider = this.getProvider();
          final String verb = arguments.getVerb();
          
          if(verb.equals("GetRecord"))
          {
             // Check required arguments.
             if(arguments.getIdentifier() == null)
                throw new BadArgumentException("An 'identifier' argument is required for this verb.");
             if(arguments.getMetadataPrefix() == null)
                throw new BadArgumentException("A 'metadataPrefix' argument is required for this verb.");
 
             // Check unsupported arguments.
             if(arguments.getFrom() != null || arguments.getUntil() != null || arguments.getResumptionToken() != null || arguments.getSet() != null)
                throw new BadArgumentException("Only the 'identifier' and 'metadataPrefix' arguments are valid for this verb.");
             
             // Retrieve the requested Record.
             final MetadataFormat format = this.metadataFormatMap.get(arguments.getMetadataPrefix());
             if(format == null)
                throw new CannotDisseminateFormatException();
 
             model.put("GetRecord", provider.getRecord(arguments.getIdentifier(), format));
          }
          else if(verb.equals("ListRecords") || verb.equals("ListIdentifiers"))	 
          {
         	 
         	 //log.info("ListRecords/ListIdentifiers Request / RT: " + arguments.getResumptionToken());
         	 System.out.println("ListRecords/ListIdentifiers Request / RT: " + arguments.getResumptionToken());
         	 
             // Check unsupported arguments.
             if(arguments.getIdentifier() != null)
                throw new BadArgumentException("The 'identifier' argument is not valid for this verb.");
 
             StateHolder state = null;
             
             if(StringUtils.isNotEmpty(arguments.getResumptionToken()))
             {
                if(arguments.getFrom() != null || arguments.getUntil() != null || arguments.getIdentifier() != null || arguments.getSet() != null || arguments.getMetadataPrefix() != null)
                   throw new BadArgumentException("A 'resumptionToken' argument cannot be used in conjunction with any others.");
 
                state = new StateHolder(arguments.getResumptionToken()); // el estado se obtiene del RT
             }
             else
             {
                // Check required arguments.
                if(StringUtils.isEmpty(arguments.getMetadataPrefix()))
                   throw new BadArgumentException("A 'metadataPrefix' argument is required for this verb."); 
                
                state = new StateHolder(); // si no hay rt  es un estado inicial
             }
 
             // list records actualiza el state, que queda conteniendo la información para generar RT
             List<Record> records = provider.listRecords(arguments.getSet(), state, !verb.equals("ListIdentifiers"));
 
             model.put("ResumptionToken", state.getResumptionToken());
             model.put("ListRecords", records);
          }
          else if(verb.equals("ListSets"))
          {
             // Check unsupported arguments.
             if(arguments.getFrom() != null || arguments.getUntil() != null || arguments.getSet() != null || arguments.getIdentifier() != null || arguments.getMetadataPrefix() != null)
                throw new BadArgumentException("No arguments are allowed for this verb other than an optional 'resumptionToken'.");
 
             // TODO: This model dispenses with the ability to use resumption tokens
             // to iterate over sets, which may not be a bad thing.
             model.put("ListSets", provider.listSets());
          }
          else if(verb.equals("Identify"))
          {
             if(requestParameters.size() > 1)
                throw new BadArgumentException("No additional arguments are allowed for this verb.");
             
             model.put("Origins", provider.listOrigins());
          }
          else if(verb.equals("ListMetadataFormats"))
          {
             // Check required arguments.
             if(arguments.getFrom() != null || arguments.getUntil() != null || arguments.getResumptionToken() != null || arguments.getSet() != null || arguments.getMetadataPrefix() != null)
                throw new BadArgumentException("Only the 'identifier' argument (or no argument) is valid for this verb.");
 
             // Get the list of allowed metadata formats.
             List<MetadataFormat> formats = null;
             /*
             if(arguments.getIdentifier() != null)
             {
                formats = new ArrayList<MetadataFormat>();
 
                // Collect matching prefixes and populate a List with matching
                // MetadataFormat instances.
                final String[] prefixes = provider.getMetadataPrefixes(arguments.getIdentifier());
                for(String prefix : prefixes)
                {
                   final MetadataFormat format = metadataFormatMap.get(prefix);
                   if(format != null)
                      formats.add(format);
                }
             }
             else*/
                formats = this.getMetadataFormats();
                
             if(formats != null && formats.size() == 0)
                throw new NoMetadataFormatsException();
 
             model.put("ListMetadataFormats", formats);
          }
          else
          {
             throw new BadArgumentException("The 'verb' argument must be one of (GetRecord|Identify|ListIdentifiers|ListMetadataFormats|ListRecords|ListSets).");
          }
          
    		return new ModelAndView("oai/" + verb, model);
       }
       catch(ProtocolException e)
       {
     	  
     	 //log.warn("Protocol error", e);
  
          model.put("ErrorCode", e.getCode());
          model.put("ErrorMessage", e.getMessage());
    		 return new ModelAndView("oai/Error", model);
    		 
       }
       catch(ServerException e)
       {
          try
          {
             //log.warn("OAI Service Error: " + e.getMessage(), e);
             response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAI Service Error: " + e.getMessage());
          }
          catch(IOException ioe)
          {
             //log.warn("Could not send error to client", ioe);
          }
       }
       catch(Throwable th)
       {
          try
          {
             //log.warn("Unexpected Error", th);
             response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error");
          }
          catch(IOException ioe)
          {
             //log.warn("Could not send error to client", ioe);
          }
       }
 
 		return new ModelAndView("oai/Error", model);
 	}
 }

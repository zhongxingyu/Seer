 package br.una.laboratorio.sgate.controller;
 
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import br.ufla.lemaf.commons.model.service.to.MessageReturnTO;
 import br.ufla.lemaf.commons.model.service.to.ObjectAndMessageReturnTO;
 import br.ufla.lemaf.commons.model.service.to.ReturnTO;
 import br.una.laboratorio.sgate.model.domain.entity.Servico;
 import br.una.laboratorio.sgate.model.service.bo.ServicoBO;
 import br.una.laboratorio.util.ContentBody;
 
 @Named
 @RequestMapping( value = "/servico/**" )
 public class ServicoController extends ApplicationController {
 
 	@Inject
 	private ServicoBO bo;
 
	@RequestMapping( value = "/servico", method = RequestMethod.POST )
 	public ReturnTO save( HttpServletRequest request ) {
 		Servico servico = ContentBody.entity(request, Servico.class);
 		return new ObjectAndMessageReturnTO<Servico>( bo.save( servico ) );
 	}
 
 	@RequestMapping( value = "/servico/{id}", method = RequestMethod.GET )
 	public ReturnTO retrieve( @PathVariable Long id ) {
 		return new ObjectAndMessageReturnTO<Servico>( bo.retrieve(id) );
 	}
 	
 	@RequestMapping(value = "/servico", method = RequestMethod.GET)
 	public ReturnTO findAll() {
 		return new ObjectAndMessageReturnTO<List<Servico>>( bo.retrieve() );
 	}
 
 	@RequestMapping(value = "/servico", method = RequestMethod.DELETE)
 	public ReturnTO delete( HttpServletRequest request ) {
 		Servico servico = ContentBody.entity(request, Servico.class);
 		bo.delete(servico);
 		return new MessageReturnTO();
 	}
 
 }

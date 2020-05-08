 /**
  * Copyright (c) 20012-2013 "FGV - CEPESP" [http://cepesp.fgv.br]
  *
  * This file is part of CEPESP-DATA.
  *
  * CEPESP-DATA is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CEPESP-DATA is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with CEPESP-DATA. If not, see <http://www.gnu.org/licenses/>.
  */
 package br.fgv.controller;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.google.common.base.Joiner;
 
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import br.com.caelum.vraptor.interceptor.download.Download;
 import br.com.caelum.vraptor.interceptor.download.FileDownload;
 import br.com.caelum.vraptor.view.Results;
 import br.fgv.CepespDataException;
 import br.fgv.business.AgregacaoPolitica;
 import br.fgv.business.AgregacaoRegional;
 import br.fgv.business.BusinessImpl;
 import br.fgv.business.FormResultAux;
 import br.fgv.model.TSEDadosAuxiliares;
 import br.fgv.util.ArgumentosBusca;
 
 @Resource
 public class ConsultaResultadosController {
 	
 	private static final Logger LOGGER = Logger.getLogger(ConsultaResultadosController.class);
 
 	private final Result result;
 	private final BusinessImpl business;
 
 	public ConsultaResultadosController(Result result, BusinessImpl business) {
 		this.result = result;
 		this.business = business;
 	}
 
 	@Get
 	@Path(priority = 1, value = "/consultaResultados")
 	public void inicial() {
 
 //		result.include("anoEleicaoList", business.getAnosDisponiveis());
 		result.include("nivelAgregacaoRegionalList", TSEDadosAuxiliares
 				.getNivelAgregacaoRegional());
 		result.include("nivelAgregacaoPoliticaList", TSEDadosAuxiliares
 				.getNivelAgregacaoPolitica());
 		result.include("filtroCargoList", business.getCargosDisponiveis());
 		
 		// XXX Porque? Deveria bloquear busca por partidos se um ano não estiver
 		// selecionado
 		result.include("filtroPartidoList", business.getPartidos("2002"));
 	}
 
 	@Get
 	@Path("/consulta/camposDisponiveis")
 	public void camposDisponiveisList(String nivelAgregacaoRegional,
 			String nivelAgregacaoPolitica) {
 		
 		final FormResultAux f = business.getCamposDisponiveis(nivelAgregacaoRegional,
 				nivelAgregacaoPolitica);
 		
 		if(LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Nivel regional: " + nivelAgregacaoRegional +
 					" Nivel politica: " + nivelAgregacaoPolitica +
 					" Campos disponíveis:\n" + f);
 		}
 		
 		result.use(Results.json()).from(f)
 			.include("camposOpcionais","camposFixos")
 			.serialize();
 		
 	}
 
 	@Get
 	@Path("/consulta/cargos")
 	public void cargosPorAno(String ano) {
 		if(LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Cargos para o ano.: " + ano);
 		}
 		result.use(Results.json()).from(business.getCargosPorAno(ano))
 				.serialize();
 	}
 	
 	@Get
 	@Path("/consulta/anos")
 	public void anosParaCargo(String cargo) {
 		if(LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Anos para o cargo: " + cargo);
 		}
 		result.use(Results.json()).from(business.getAnosParaCargo(cargo))
 				.serialize();
 	}
 
 	@Get
 	@Path("/consulta/partidos")
 	public void partidosPorAno(String ano) {
 		if(LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Partidos para o ano.: " + ano);
 		}
 		result.use(Results.json()).from(business.getPartidos(ano))
 				.serialize();
 	}
 
 	@Get
 	@Path("/consulta/partidosAnos")
 	public void partidosPorAno(String[] anosList) {
 		if(LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Partidos para o ano.: " + Arrays.toString(anosList));
 		}
 		result.use(Results.json()).from(business.getPartidos(anosList))
 				.serialize();
 	}
 
 	@Get
 	@Path("/consulta/candidatos")
 	public void candidatosPorAno(String q, String ano) {
 		if(LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Candidatos para o ano. Ano: " + ano + "; Filtro: " + q);
 		}
 		result.use(Results.json()).from(business.getCandidatos(q, ano))
 				.serialize();
 	}
 
 	@Get
 	@Path("/consulta/candidatosAnosCargo")
 	public void candidatosPorAno(String q, String[] anosList, String cargo) {
 		if(LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Candidatos para o ano. Anos: " + Arrays.toString(anosList) + "; Filtro: " + q + "; Cargo: " + cargo);
 		}
 		result.use(Results.json()).from(business.getCandidatos(q, anosList, cargo))
 				.serialize();
 	}
 
 	@Get
 	@Path("/consulta/filtroRegionalQuery")
 	public void filtroRegionalQuery(String q, String nivelRegional) {
 		if(LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Filtro Query................: " + q);
 			LOGGER.debug("Filtro para o nivel Regional: " + nivelRegional);
 		}
 		result.use(Results.json()).from(
 				business.getFiltroRegional(q, nivelRegional)).serialize();
 	}
 
 	@Post
 	@Path("/resultados.csv")
 	public Download resultadosCSVEntrada(List<String> anosEscolhidos, String filtroCargo,
 			String nivelAgregacaoRegional, String nivelAgregacaoPolitica,
 			List<String> camposEscolhidos, List<String> camposFixos,
 			String nivelFiltroRegional, String as_values_regional,
 			String as_values_candidatos, String as_values_partidos)
 			throws CepespDataException {
 
 		if(LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Preparando para criar CSV");
 			LOGGER.debug("as_values_partidos:" + as_values_partidos);
 			LOGGER.debug("as_values_regional:" + as_values_regional);
 		}
 		
 		List<String> fp = trataLista(as_values_partidos);
 		List<String> fr = trataLista(as_values_regional);
 		List<String> fc = trataLista(as_values_candidatos);
 		
 
 		return resultadosCSV(anosEscolhidos, filtroCargo, nivelAgregacaoRegional,
 				nivelAgregacaoPolitica, camposEscolhidos, camposFixos,
 				nivelFiltroRegional, fr, fp, fc);
 	}
 
 	private List<String> trataLista(String lista) {
 	    lista = lista == null ? "": lista;
 		String[] tmp = lista.split(",");
 		List<String> ret = new ArrayList<String>();
 
 		for (int i = 0; i < tmp.length; i++) {
 			if (tmp[i].trim() != "") {
 				ret.add(tmp[i]);
 			}
 		}
 
 		return ret;
 	}
 
 	// @Post
 	// @Path("/resultados.csv")
 	public Download resultadosCSV(List<String> anosEscolhidos, String filtroCargo,
 			String nivelAgregacaoRegional, String nivelAgregacaoPolitica,
 			List<String> camposEscolhidos, List<String> camposFixos,
 			String nivelFiltroRegional, List<String> filtroRegional,
 			List<String> filtroPartido, List<String> filtroCandidato) throws CepespDataException {
 		
 		long start = -1;
		if(LOGGER.isDebugEnabled()) {
 			LOGGER.debug(">>> resultadosCSV " + Arrays.toString(camposEscolhidos.toArray()));
 			start = System.currentTimeMillis();
 		}
 		
 		
 		camposFixos = camposFixos == null ? Collections.<String>emptyList()
 				: camposFixos;
 		camposEscolhidos = camposEscolhidos == null ? Collections.<String>emptyList()
 				: camposEscolhidos;
 		filtroRegional = filtroRegional == null ? Collections.<String>emptyList()
 				: filtroRegional;
 		filtroPartido = filtroPartido == null ? Collections.<String>emptyList()
 				: filtroPartido;
 		filtroCandidato = filtroCandidato == null ? Collections.<String>emptyList()
 				: filtroCandidato;
 		
 		List<String> c = new ArrayList<String>();
 		c.addAll(camposFixos);
 		c.addAll(camposEscolhidos);
 		String[] campos = c.toArray(new String[c.size()]);
 		
 		ArgumentosBusca args = new ArgumentosBusca();
 		Collections.sort(anosEscolhidos);
 		args.setAnoEleicao(anosEscolhidos.toArray(new String[anosEscolhidos.size()]));
 		args.setFiltroCargo(filtroCargo);
 		args.setNivelAgrecacaoPolitica(AgregacaoPolitica.fromInt(nivelAgregacaoPolitica));
 		args.setNivelRegional(AgregacaoRegional.fromInt(nivelAgregacaoRegional));
 		args.setCamposEscolhidos(campos);
 		
 		
 		args.setNivelFiltroRegional(AgregacaoRegional.fromInt(nivelFiltroRegional));
 		
 
 		String[] fr = filtroRegional.toArray(new String[filtroRegional.size()]);
 		String[] fp = filtroPartido.toArray(new String[filtroPartido.size()]);
 		String[] fc = filtroCandidato
 				.toArray(new String[filtroCandidato.size()]);
 
 		args.setFiltroRegional(fr);
 		args.setFiltroPartido(fp);
 		args.setFiltroCandidato(fc);
 		
 		if(LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Argumentos da busca: " + args.toString());
 		}
 		
 		File retFile = business.getLinkResult(args);
 
 		
 		String nameFile = business.getSugestaoNomeArquivo(Joiner.on("-").join(anosEscolhidos),
 				nivelAgregacaoRegional, nivelAgregacaoPolitica, filtroCargo);
 
 		if(LOGGER.isDebugEnabled()) {
 			
 			LOGGER.debug("<<< resultadosCSV. Tempo(s): " + (System.currentTimeMillis() - start)/1000.0);
 			
 		}		
 		
 		return new FileDownload(retFile, "text/csv", nameFile, true);
 	}
 
 }

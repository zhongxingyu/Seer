 package controllers;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import models.Bolsa;
 import models.Maquina;
 import models.MaquinaAdulto;
 import models.MaquinaAventura;
 import models.MaquinaInfantil;
 import models.MaquinaReflex;
 import models.MaterialFoto;
 import models.Objetiva;
 import models.PropertiesTDB;
 
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.tdb.TDBFactory;
 
 public class Search {
 
 	final String NS = "http://www.owl-ontologies.com/maquinas.owl#";
 
 	public List<MaterialFoto> search(String type, String scope, String value) {
 
 		String directory = "database";
 		Dataset dataset = TDBFactory.createDataset(directory);
 
 		Model tdb = dataset.getDefaultModel();
 		PropertiesTDB props = new PropertiesTDB(tdb, NS);
 
 		String q;
 		if(scope.equals("all")) {
 			q =  "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
 					"PREFIX xmlns: <http://www.owl-ontologies.com/maquinas.owl#> " +
 					"select ?element " +
 					"where {?element rdf:type xmlns:" + type + "} ";
 		}
 		else if(scope.equals("marca")) {
 			q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
 					"PREFIX xmlns: <http://www.owl-ontologies.com/maquinas.owl#> " +
 					"select ?element " +
 							"where {" +
 							"?element rdf:type xmlns:" + type + " . " +
 							"?element xmlns:Marca \"" + value + "\"" +
 							"} ";
 		}
 		else {
 			q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
 					"PREFIX xmlns: <http://www.owl-ontologies.com/maquinas.owl#> " +
 					"select ?element " +
 							"where {" +
 							"?element rdf:type xmlns:" + type + " . " +
 							"?element xmlns:Id \"" + value + "\"" +
 							"} ";
 		}
 
 		Query query = QueryFactory.create(q);
 		QueryExecution qexec = QueryExecutionFactory.create(query,tdb);
 		ResultSet results = qexec.execSelect();
 
 		List<MaterialFoto> lista = new ArrayList<MaterialFoto>();
 		while(results.hasNext()) {
 			MaterialFoto matFoto = null;
 			QuerySolution sol = results.nextSolution();
 			if(type.equals("Aventura")) {
 				matFoto = new MaquinaAventura();
 				((MaquinaAventura)matFoto).setZoom(trimQuote(sol.get("element").asResource().getProperty(props.zoom).asTriple().getMatchObject().toString()));
 				((MaquinaAventura)matFoto).setAbertura(trimQuote(sol.get("element").asResource().getProperty(props.abertLente).asTriple().getMatchObject().toString()));
 				((MaquinaAventura)matFoto).setIso(trimQuote(sol.get("element").asResource().getProperty(props.iso).asTriple().getMatchObject().toString()));
 				((MaquinaAventura)matFoto).setVelocidade(trimQuote(sol.get("element").asResource().getProperty(props.velo).asTriple().getMatchObject().toString()));
 			}
 			else if(type.equals("Reflex")) {
 				matFoto = new MaquinaReflex();
 				((MaquinaReflex)matFoto).setObjetiva(trimQuote(sol.get("element").asResource().getProperty(props.objIncl).asTriple().getMatchObject().toString()));
 				((MaquinaReflex)matFoto).setNivelUser(trimQuote(sol.get("element").asResource().getProperty(props.nivUser).asTriple().getMatchObject().toString()));
 
 			}
 
 			if(type.equals("Aventura") || type.equals("Reflex")) { //adulto
 				((MaquinaAdulto)matFoto).setFormVideo(trimQuote(sol.get("element").asResource().getProperty(props.formVid).asTriple().getMatchObject().toString()));
 				((MaquinaAdulto)matFoto).setFotoSeq(trimQuote(sol.get("element").asResource().getProperty(props.fotoSeq).asTriple().getMatchObject().toString()));
 			}
 
 			if(type.equals("Infatil")) {
 				matFoto = new MaquinaInfantil();
 				((MaquinaInfantil)matFoto).setCor(trimQuote(sol.get("element").asResource().getProperty(props.cor).asTriple().getMatchObject().toString()));
 			}
 
 			if(type.equals("Aventura") || type.equals("Reflex") || type.equals("Infantil")) { //maquina
 				((Maquina)matFoto).setBateria(trimQuote(sol.get("element").asResource().getProperty(props.bateria).asTriple().getMatchObject().toString()));
 				((Maquina)matFoto).setDimensao(trimQuote(sol.get("element").asResource().getProperty(props.dimensao).asTriple().getMatchObject().toString()));
 				((Maquina)matFoto).setDimensaoEcra(trimQuote(sol.get("element").asResource().getProperty(props.dimEcra).asTriple().getMatchObject().toString()));
 				((Maquina)matFoto).setDistFocal(trimQuote(sol.get("element").asResource().getProperty(props.distFocalMaq).asTriple().getMatchObject().toString()));
 				((Maquina)matFoto).setInterf(trimQuote(sol.get("element").asResource().getProperty(props.interf).asTriple().getMatchObject().toString()));
 				((Maquina)matFoto).setPeso(trimQuote(sol.get("element").asResource().getProperty(props.pesoCorpo).asTriple().getMatchObject().toString()));
 				((Maquina)matFoto).setResolucao(trimQuote(sol.get("element").asResource().getProperty(props.resolucao).asTriple().getMatchObject().toString()));
 				((Maquina)matFoto).setTipoMem(trimQuote(sol.get("element").asResource().getProperty(props.memoria).asTriple().getMatchObject().toString()));
 
 			}
 
 			if(type.equals("Objetiva")) {
 				matFoto = new Objetiva();
 				((Objetiva)matFoto).setDistFoc(trimQuote(sol.get("element").asResource().getProperty(props.distFocal).asTriple().getMatchObject().toString()));
 				((Objetiva)matFoto).setAbertura(trimQuote(sol.get("element").asResource().getProperty(props.abertura).asTriple().getMatchObject().toString()));
 				((Objetiva)matFoto).setDimensoes(trimQuote(sol.get("element").asResource().getProperty(props.dimensoes).asTriple().getMatchObject().toString()));
 				((Objetiva)matFoto).setPeso(trimQuote(sol.get("element").asResource().getProperty(props.peso).asTriple().getMatchObject().toString()));
 				((Objetiva)matFoto).setTipo(trimQuote(sol.get("element").asResource().getProperty(props.tipo).asTriple().getMatchObject().toString()));
 			}
 
 			if(type.equals("Bolsas")) {
 				matFoto = new Bolsa();
 				((Bolsa)matFoto).setDimInterior(trimQuote(sol.get("element").asResource().getProperty(props.dimInt).asTriple().getMatchObject().toString()));
 				((Bolsa)matFoto).setDimExterior(trimQuote(sol.get("element").asResource().getProperty(props.dimExt).asTriple().getMatchObject().toString()));
 			}
 
 			matFoto.setTitulo(trimQuote(sol.get("element").asResource().getProperty(props.titulo).asTriple().getMatchObject().toString()));
 			matFoto.setMarca(trimQuote(sol.get("element").asResource().getProperty(props.marca).asTriple().getMatchObject().toString()));
 			matFoto.setPreco(trimQuote(sol.get("element").asResource().getProperty(props.preco).asTriple().getMatchObject().toString()));
			matFoto.setId(Integer.parseInt(trimQuote(sol.get("element").asResource().getProperty(props.id).asTriple().getMatchObject().toString())));
 
 			lista.add(matFoto);
 		}
 
         qexec.close();
 
         return lista;
 	}
 
 	public List<MaquinaAventura> searchAventura() {
 		List<MaquinaAventura> aventura = new ArrayList<MaquinaAventura>();
 		List<MaterialFoto> lista = search("Aventura","all","");
 		for (MaterialFoto m: lista) {
 			aventura.add((MaquinaAventura)m);
 		}
 		return aventura;
 	}
 
 	public List<MaquinaReflex> searchReflex() {
 		List<MaquinaReflex> reflex = new ArrayList<MaquinaReflex>();
 		List<MaterialFoto> lista = search("Reflex","all","");
 		for (MaterialFoto m: lista) {
 			reflex.add((MaquinaReflex)m);
 		}
 		return reflex;
 	}
 
 	public List<MaquinaInfantil> searchInfantil() {
 		List<MaquinaInfantil> infantil = new ArrayList<MaquinaInfantil>();
 		List<MaterialFoto> lista = search("Infatil","all","");
 		for (MaterialFoto m: lista) {
 			infantil.add((MaquinaInfantil)m);
 		}
 		return infantil;
 	}
 
 	public List<Objetiva> searchObjetiva() {
 		List<Objetiva> objetiva = new ArrayList<Objetiva>();
 		List<MaterialFoto> lista = search("Objetiva","all","");
 		for (MaterialFoto m: lista) {
 			objetiva.add((Objetiva)m);
 		}
 		return objetiva;
 	}
 
 	public List<Bolsa> searchBolsa() {
 		List<Bolsa> bolsa = new ArrayList<Bolsa>();
 		List<MaterialFoto> lista = search("Bolsas","all","");
 		for (MaterialFoto m: lista) {
 			bolsa.add((Bolsa)m);
 		}
 		return bolsa;
 	}
 
 	public String trimQuote(String str) {
 		return str.substring(1, str.length()-1);
 	}
 }

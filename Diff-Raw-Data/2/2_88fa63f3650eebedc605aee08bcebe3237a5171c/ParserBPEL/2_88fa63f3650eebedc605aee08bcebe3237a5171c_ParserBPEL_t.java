 package br.ipt.servico.relevancia.transformacao;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.log4j.Logger;
 
 import br.ipt.servico.relevancia.bpel.ProcessoNegocio;
 import br.ipt.servico.relevancia.modelo.TActivity;
 import br.ipt.servico.relevancia.modelo.TActivityContainer;
 import br.ipt.servico.relevancia.modelo.TAssign;
 import br.ipt.servico.relevancia.modelo.TBoolean;
 import br.ipt.servico.relevancia.modelo.TCompensate;
 import br.ipt.servico.relevancia.modelo.TEmpty;
 import br.ipt.servico.relevancia.modelo.TExtensibleElements;
 import br.ipt.servico.relevancia.modelo.TFlow;
 import br.ipt.servico.relevancia.modelo.TInvoke;
 import br.ipt.servico.relevancia.modelo.TOnAlarm;
 import br.ipt.servico.relevancia.modelo.TOnMessage;
 import br.ipt.servico.relevancia.modelo.TPick;
 import br.ipt.servico.relevancia.modelo.TProcess;
 import br.ipt.servico.relevancia.modelo.TReceive;
 import br.ipt.servico.relevancia.modelo.TReply;
 import br.ipt.servico.relevancia.modelo.TScope;
 import br.ipt.servico.relevancia.modelo.TSequence;
 import br.ipt.servico.relevancia.modelo.TSwitch;
 import br.ipt.servico.relevancia.modelo.TTerminate;
 import br.ipt.servico.relevancia.modelo.TThrow;
 import br.ipt.servico.relevancia.modelo.TWait;
 import br.ipt.servico.relevancia.modelo.TWhile;
 import br.ipt.servico.relevancia.multidigrafo.Arco;
 import br.ipt.servico.relevancia.multidigrafo.Vertice;
 import br.ipt.servico.relevancia.multidigrafo.VerticeFim;
 import br.ipt.servico.relevancia.multidigrafo.VerticeInicio;
 import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
 import edu.uci.ics.jung.graph.Graph;
 
 /**
  * Mapeia um conjunto de processos de negocio BPEL para um multidigrafo.
  * 
  * @author Rodrigo Mendes Leme
  */
 public class ParserBPEL {
 
     private Logger log = Logger.getLogger(ParserBPEL.class);
 
     private VerticeInicio verticeInicioPN;
 
     public Graph<Vertice, Arco> parse(List<ProcessoNegocio> processosNegocio) {
 	if (processosNegocio == null) {
 	    return new DirectedSparseMultigraph<Vertice, Arco>();
 	}
 
 	Graph<Vertice, Arco> grafo = new DirectedSparseMultigraph<Vertice, Arco>();
 	for (ProcessoNegocio processoNegocio : processosNegocio) {
 	    try {
 		this.log.info("Gerando o multidigrafo do processo de negocio "
 			+ processoNegocio.getNome() + ".");
 		TProcess process = JAXBContext.newInstance(TProcess.class)
 			.createUnmarshaller().unmarshal(
 				new StreamSource(
 					new StringReader(processoNegocio
 						.getBpel().getConteudo())),
 				TProcess.class).getValue();
 
 		this.verticeInicioPN = null;
 		this.buscaEmProfundidade(grafo, processoNegocio, process
 			.getSequence(), null);
 		this.log.debug(grafo);
 		this.log.info("Multidigrafo do processo de negocio "
 			+ processoNegocio.getNome() + " gerado com sucesso ("
 			+ grafo.getVertexCount() + " vertices e "
 			+ grafo.getEdgeCount() + " arcos).");
 	    } catch (JAXBException e) {
 		grafo = null;
 		this.log.error(
 			"Erro: nao conseguiu fazer unmarshalling do processo de negocio "
 				+ processoNegocio.getNome() + ".", e);
 	    }
 	}
 
 	this.refinarVerticesCompostos(grafo, processosNegocio);
 
 	return grafo;
     }
 
     private List<Vertice> buscaEmProfundidade(Graph<Vertice, Arco> grafo,
 	    ProcessoNegocio processoNegocio,
 	    TExtensibleElements tExtensibleElements,
 	    List<Vertice> verticesAnteriores) {
 	if (tExtensibleElements == null) {
 	    return null;
 	}
 
 	if (tExtensibleElements instanceof TActivity) {
 	    if (tExtensibleElements instanceof TReceive) {
 		return this.mapearReceive((TReceive) tExtensibleElements,
 			grafo, processoNegocio, verticesAnteriores);
 	    } else if (tExtensibleElements instanceof TReply) {
 		return this.mapearReply((TReply) tExtensibleElements, grafo,
 			processoNegocio, verticesAnteriores);
 	    } else if (tExtensibleElements instanceof TInvoke) {
 		return this.mapearInvoke((TInvoke) tExtensibleElements, grafo,
 			processoNegocio, verticesAnteriores);
 	    } else if (tExtensibleElements instanceof TTerminate) {
 		return this.mapearTerminate((TTerminate) tExtensibleElements,
 			grafo, processoNegocio, verticesAnteriores);
 	    } else if (tExtensibleElements instanceof TSequence) {
 		return this.mapearSequence((TSequence) tExtensibleElements,
 			grafo, processoNegocio, verticesAnteriores);
 	    } else if (tExtensibleElements instanceof TSwitch) {
 		return this.mapearSwitch((TSwitch) tExtensibleElements, grafo,
 			processoNegocio, verticesAnteriores);
 	    } else if (tExtensibleElements instanceof TWhile) {
 		return this.mapearWhile((TWhile) tExtensibleElements, grafo,
 			processoNegocio, verticesAnteriores);
 	    } else if (tExtensibleElements instanceof TPick) {
 		return this.mapearPick((TPick) tExtensibleElements, grafo,
 			processoNegocio, verticesAnteriores);
 	    } else if (tExtensibleElements instanceof TFlow) {
 		return this.mapearFlow((TFlow) tExtensibleElements, grafo,
 			processoNegocio, verticesAnteriores);
 	    } else if (tExtensibleElements instanceof TAssign) {
 		return this.mapearAssign((TAssign) tExtensibleElements, grafo,
 			processoNegocio, verticesAnteriores);
 	    } else if (tExtensibleElements instanceof TWait) {
 		return this.mapearWait((TWait) tExtensibleElements, grafo,
 			processoNegocio, verticesAnteriores);
 	    } else if (tExtensibleElements instanceof TEmpty) {
 		return this.mapearEmpty((TEmpty) tExtensibleElements, grafo,
 			processoNegocio, verticesAnteriores);
 	    } else if (tExtensibleElements instanceof TThrow) {
 		return this.mapearThrow((TThrow) tExtensibleElements, grafo,
 			processoNegocio, verticesAnteriores);
 	    } else if (tExtensibleElements instanceof TCompensate) {
 		return this.mapearCompensate((TCompensate) tExtensibleElements,
 			grafo, processoNegocio, verticesAnteriores);
 	    } else if (tExtensibleElements instanceof TScope) {
 		return this.mapearScope((TScope) tExtensibleElements, grafo,
 			processoNegocio, verticesAnteriores);
 	    } else {
 		return verticesAnteriores;
 	    }
 	} else {
 	    return verticesAnteriores;
 	}
     }
 
     private List<Vertice> mapearReceive(TReceive tReceive,
 	    Graph<Vertice, Arco> grafo, ProcessoNegocio processoNegocio,
 	    List<Vertice> verticesAnteriores) {
 	if (tReceive.getCreateInstance().equals(TBoolean.YES)) {
 	    if (this.verticeInicioPN == null) {
 		Map<Vertice.Rotulo, String> rotulosVertice = new HashMap<Vertice.Rotulo, String>();
 		rotulosVertice.put(Vertice.Rotulo.ID_PROCESSO_NEGOCIO, String
 			.valueOf(processoNegocio.getId()));
 		VerticeInicio verticeInicio = new VerticeInicio(rotulosVertice);
 		grafo.addVertex(verticeInicio);
 		this.verticeInicioPN = verticeInicio;
 	    }
 	    this.log.debug("Elemento encontrado: <receive> ("
 		    + tReceive.getName() + ") --> vertice \""
 		    + this.verticeInicioPN.toString() + "\".");
 	    return Arrays.asList(new Vertice[] { this.verticeInicioPN });
 	} else {
 	    this.log.debug("Elemento encontrado: <receive> ("
 		    + tReceive.getName() + ").");
 	    return verticesAnteriores;
 	}
     }
 
     private List<Vertice> mapearReply(TReply tReply,
 	    Graph<Vertice, Arco> grafo, ProcessoNegocio processoNegocio,
 	    List<Vertice> verticesAnteriores) {
 	Map<Vertice.Rotulo, String> rotulosVertice = new HashMap<Vertice.Rotulo, String>();
 	rotulosVertice.put(Vertice.Rotulo.ID_PROCESSO_NEGOCIO, String
 		.valueOf(processoNegocio.getId()));
 	VerticeFim verticeFim = new VerticeFim(rotulosVertice);
 	grafo.addVertex(verticeFim);
 
 	this.log.debug("Elemento encontrado: <reply> (" + tReply.getName()
 		+ ") --> vertice \"" + verticeFim.toString() + "\".");
 
 	for (Vertice verticeAnterior : verticesAnteriores) {
 	    Map<Arco.Rotulo, String> rotulosArco = new HashMap<Arco.Rotulo, String>();
 	    rotulosArco.put(Arco.Rotulo.ID_PROCESSO_NEGOCIO, String
 		    .valueOf(processoNegocio.getId()));
 	    rotulosArco.put(Arco.Rotulo.NUMERO_INVOCACOES, "0");
 	    Arco arco = new Arco(rotulosArco);
 	    grafo.addEdge(arco, verticeAnterior, verticeFim);
 	}
 
 	return Arrays.asList(new Vertice[] { verticeFim });
     }
 
     private List<Vertice> mapearInvoke(TInvoke tInvoke,
 	    Graph<Vertice, Arco> grafo, ProcessoNegocio processoNegocio,
 	    List<Vertice> verticesAnteriores) {
 	Map<Vertice.Rotulo, String> rotulosVertice = new HashMap<Vertice.Rotulo, String>();
 	rotulosVertice = new HashMap<Vertice.Rotulo, String>();
 	rotulosVertice.put(Vertice.Rotulo.TEMPO_MEDIO_EXECUCAO, "0");
 	rotulosVertice.put(Vertice.Rotulo.TEMPO_RESPOSTA_ESPERADO, "0");
 	rotulosVertice.put(Vertice.Rotulo.SERVICO_OPERACAO, tInvoke
 		.getPartnerLink()
 		+ "." + tInvoke.getOperation());
 	rotulosVertice.put(Vertice.Rotulo.URL, processoNegocio
		.obterURLPartnerLink(tInvoke.getPartnerLink()));
 	Vertice vertice = new Vertice(rotulosVertice);
 
 	if (grafo.containsVertex(vertice)) {
 	    for (Vertice v : grafo.getVertices()) {
 		if (v.equals(vertice)) {
 		    vertice = v;
 		    break;
 		}
 	    }
 	} else {
 	    grafo.addVertex(vertice);
 	}
 
 	this.log.debug("Elemento encontrado: <invoke> (" + tInvoke.getName()
 		+ ") --> vertice \"" + vertice.toString() + "\".");
 
 	for (Vertice verticeAnterior : verticesAnteriores) {
 	    Map<Arco.Rotulo, String> rotulosArco = new HashMap<Arco.Rotulo, String>();
 	    rotulosArco.put(Arco.Rotulo.ID_PROCESSO_NEGOCIO, String
 		    .valueOf(processoNegocio.getId()));
 	    rotulosArco.put(Arco.Rotulo.NUMERO_INVOCACOES, "0");
 	    Arco arco = new Arco(rotulosArco);
 	    grafo.addEdge(arco, verticeAnterior, vertice);
 	}
 
 	return Arrays.asList(new Vertice[] { vertice });
     }
 
     private List<Vertice> mapearTerminate(TTerminate tTerminate,
 	    Graph<Vertice, Arco> grafo, ProcessoNegocio processoNegocio,
 	    List<Vertice> verticesAnteriores) {
 	Map<Vertice.Rotulo, String> rotulosVertice = new HashMap<Vertice.Rotulo, String>();
 	rotulosVertice.put(Vertice.Rotulo.ID_PROCESSO_NEGOCIO, String
 		.valueOf(processoNegocio.getId()));
 	VerticeFim verticeFim = new VerticeFim(rotulosVertice);
 	grafo.addVertex(verticeFim);
 
 	this.log.debug("Elemento encontrado: <terminate> ("
 		+ tTerminate.getName() + ") --> vertice \""
 		+ verticeFim.toString() + "\".");
 
 	for (Vertice verticeAnterior : verticesAnteriores) {
 	    Map<Arco.Rotulo, String> rotulosArco = new HashMap<Arco.Rotulo, String>();
 	    rotulosArco.put(Arco.Rotulo.ID_PROCESSO_NEGOCIO, String
 		    .valueOf(processoNegocio.getId()));
 	    rotulosArco.put(Arco.Rotulo.NUMERO_INVOCACOES, "0");
 	    Arco arco = new Arco(rotulosArco);
 	    grafo.addEdge(arco, verticeAnterior, verticeFim);
 	}
 
 	return Arrays.asList(new Vertice[] { verticeFim });
     }
 
     private List<Vertice> mapearSequence(TSequence tSequence,
 	    Graph<Vertice, Arco> grafo, ProcessoNegocio processoNegocio,
 	    List<Vertice> verticesAnteriores) {
 	this.log.debug("Elemento encontrado: <sequence> ("
 		+ tSequence.getName() + ").");
 
 	List<Vertice> vertices = verticesAnteriores;
 	for (TActivity tActivity : tSequence.getActivity()) {
 	    vertices = this.buscaEmProfundidade(grafo, processoNegocio,
 		    tActivity, vertices);
 	}
 
 	return vertices;
     }
 
     private List<Vertice> mapearSwitch(TSwitch tSwitch,
 	    Graph<Vertice, Arco> grafo, ProcessoNegocio processoNegocio,
 	    List<Vertice> verticesAnteriores) {
 	this.log.debug("Elemento encontrado: <switch> (" + tSwitch.getName()
 		+ ").");
 
 	List<Vertice> vertices = new ArrayList<Vertice>();
 	for (TSwitch.Case tCase : tSwitch.getCase()) {
 	    vertices.addAll(this.buscaEmProfundidade(grafo, processoNegocio,
 		    this.obterElementoFilho(tCase), verticesAnteriores));
 	}
 	if (tSwitch.getOtherwise() != null) {
 	    vertices.addAll(this.buscaEmProfundidade(grafo, processoNegocio,
 		    this.obterElementoFilho(tSwitch.getOtherwise()),
 		    verticesAnteriores));
 	}
 
 	return vertices;
     }
 
     private List<Vertice> mapearWhile(TWhile tWhile,
 	    Graph<Vertice, Arco> grafo, ProcessoNegocio processoNegocio,
 	    List<Vertice> verticesAnteriores) {
 	this.log.debug("Elemento encontrado: <while> (" + tWhile.getName()
 		+ ").");
 
 	List<Vertice> vertices = this.buscaEmProfundidade(grafo,
 		processoNegocio, this.obterElementoFilho(tWhile),
 		verticesAnteriores);
 
 	Vertice verticeInicioRecorrencia = grafo.getSuccessors(
 		verticesAnteriores.get(0)).toArray(new Vertice[0])[0];
 
 	for (Vertice vertice : vertices) {
 	    Map<Arco.Rotulo, String> rotulosArco = new HashMap<Arco.Rotulo, String>();
 	    rotulosArco.put(Arco.Rotulo.ID_PROCESSO_NEGOCIO, String
 		    .valueOf(processoNegocio.getId()));
 	    rotulosArco.put(Arco.Rotulo.NUMERO_INVOCACOES, "0");
 	    Arco arco = new Arco(rotulosArco);
 	    grafo.addEdge(arco, vertice, verticeInicioRecorrencia);
 	}
 
 	return vertices;
     }
 
     private List<Vertice> mapearPick(TPick tPick, Graph<Vertice, Arco> grafo,
 	    ProcessoNegocio processoNegocio, List<Vertice> verticesAnteriores) {
 	List<Vertice> vertices = new ArrayList<Vertice>();
 	if (tPick.getCreateInstance().equals(TBoolean.NO)) {
 	    this.log.debug("Elemento encontrado: <pick> (" + tPick.getName()
 		    + ").");
 	    for (TOnMessage tOnMessage : tPick.getOnMessage()) {
 		vertices.addAll(this.buscaEmProfundidade(grafo,
 			processoNegocio, this.obterElementoFilho(tOnMessage),
 			verticesAnteriores));
 	    }
 	    for (TOnAlarm tOnAlarm : tPick.getOnAlarm()) {
 		vertices.addAll(this.buscaEmProfundidade(grafo,
 			processoNegocio, this.obterElementoFilho(tOnAlarm),
 			verticesAnteriores));
 	    }
 	} else {
 	    for (TOnMessage tOnMessage : tPick.getOnMessage()) {
 		if (this.verticeInicioPN == null) {
 		    Map<Vertice.Rotulo, String> rotulosVertice = new HashMap<Vertice.Rotulo, String>();
 		    rotulosVertice.put(Vertice.Rotulo.ID_PROCESSO_NEGOCIO,
 			    String.valueOf(processoNegocio.getId()));
 		    VerticeInicio verticeInicio = new VerticeInicio(
 			    rotulosVertice);
 		    grafo.addVertex(verticeInicio);
 		    this.verticeInicioPN = verticeInicio;
 		}
 		this.log.debug("Elemento encontrado: <pick> ("
 			+ tPick.getName() + ") --> vertice \""
 			+ this.verticeInicioPN.toString() + "\".");
 		vertices.addAll(this.buscaEmProfundidade(grafo,
 			processoNegocio, this.obterElementoFilho(tOnMessage),
 			Arrays.asList(new Vertice[] { this.verticeInicioPN })));
 	    }
 	}
 
 	return vertices;
     }
 
     private List<Vertice> mapearFlow(TFlow tFlow, Graph<Vertice, Arco> grafo,
 	    ProcessoNegocio processoNegocio, List<Vertice> verticesAnteriores) {
 	this.log
 		.debug("Elemento encontrado: <flow> (" + tFlow.getName() + ").");
 
 	List<Vertice> vertices = new ArrayList<Vertice>();
 	for (TActivity tActivity : tFlow.getActivity()) {
 	    vertices.addAll(this.buscaEmProfundidade(grafo, processoNegocio,
 		    tActivity, verticesAnteriores));
 	}
 
 	return vertices;
     }
 
     private List<Vertice> mapearAssign(TAssign tAssign,
 	    Graph<Vertice, Arco> grafo, ProcessoNegocio processoNegocio,
 	    List<Vertice> verticesAnteriores) {
 	this.log.debug("Elemento encontrado: <assign> (" + tAssign.getName()
 		+ ").");
 	return verticesAnteriores;
     }
 
     private List<Vertice> mapearWait(TWait tWait, Graph<Vertice, Arco> grafo,
 	    ProcessoNegocio processoNegocio, List<Vertice> verticesAnteriores) {
 	this.log
 		.debug("Elemento encontrado: <wait> (" + tWait.getName() + ").");
 	return verticesAnteriores;
     }
 
     private List<Vertice> mapearEmpty(TEmpty tEmpty,
 	    Graph<Vertice, Arco> grafo, ProcessoNegocio processoNegocio,
 	    List<Vertice> verticesAnteriores) {
 	this.log.debug("Elemento encontrado: <empty> (" + tEmpty.getName()
 		+ ").");
 	return verticesAnteriores;
     }
 
     private List<Vertice> mapearThrow(TThrow tThrow,
 	    Graph<Vertice, Arco> grafo, ProcessoNegocio processoNegocio,
 	    List<Vertice> verticesAnteriores) {
 	this.log.debug("Elemento encontrado: <throw> (" + tThrow.getName()
 		+ ").");
 	return verticesAnteriores;
     }
 
     private List<Vertice> mapearCompensate(TCompensate tCompensate,
 	    Graph<Vertice, Arco> grafo, ProcessoNegocio processoNegocio,
 	    List<Vertice> verticesAnteriores) {
 	this.log.debug("Elemento encontrado: <compensate> ("
 		+ tCompensate.getName() + ").");
 	return verticesAnteriores;
     }
 
     private List<Vertice> mapearScope(TScope tScope,
 	    Graph<Vertice, Arco> grafo, ProcessoNegocio processoNegocio,
 	    List<Vertice> verticesAnteriores) {
 	this.log.debug("Elemento encontrado: <scope> (" + tScope.getName()
 		+ ").");
 	return this.buscaEmProfundidade(grafo, processoNegocio, this
 		.obterElementoFilho(tScope), verticesAnteriores);
     }
 
     private TExtensibleElements obterElementoFilho(
 	    TActivityContainer tActivityContainer) {
 	if (tActivityContainer.getSequence() != null) {
 	    return tActivityContainer.getSequence();
 	} else if (tActivityContainer.getEmpty() != null) {
 	    return tActivityContainer.getEmpty();
 	} else if (tActivityContainer.getInvoke() != null) {
 	    return tActivityContainer.getInvoke();
 	} else if (tActivityContainer.getReceive() != null) {
 	    return tActivityContainer.getReceive();
 	} else if (tActivityContainer.getReply() != null) {
 	    return tActivityContainer.getReply();
 	} else if (tActivityContainer.getAssign() != null) {
 	    return tActivityContainer.getAssign();
 	} else if (tActivityContainer.getWait() != null) {
 	    return tActivityContainer.getWait();
 	} else if (tActivityContainer.getThrow() != null) {
 	    return tActivityContainer.getThrow();
 	} else if (tActivityContainer.getTerminate() != null) {
 	    return tActivityContainer.getTerminate();
 	} else if (tActivityContainer.getFlow() != null) {
 	    return tActivityContainer.getTerminate();
 	} else if (tActivityContainer.getSwitch() != null) {
 	    return tActivityContainer.getSwitch();
 	} else if (tActivityContainer.getWhile() != null) {
 	    return tActivityContainer.getWhile();
 	} else if (tActivityContainer.getPick() != null) {
 	    return tActivityContainer.getWhile();
 	} else if (tActivityContainer.getScope() != null) {
 	    return tActivityContainer.getScope();
 	} else {
 	    return null;
 	}
     }
 
     private TExtensibleElements obterElementoFilho(TWhile tWhile) {
 	if (tWhile.getSequence() != null) {
 	    return tWhile.getSequence();
 	} else if (tWhile.getEmpty() != null) {
 	    return tWhile.getEmpty();
 	} else if (tWhile.getInvoke() != null) {
 	    return tWhile.getInvoke();
 	} else if (tWhile.getReceive() != null) {
 	    return tWhile.getReceive();
 	} else if (tWhile.getReply() != null) {
 	    return tWhile.getReply();
 	} else if (tWhile.getAssign() != null) {
 	    return tWhile.getAssign();
 	} else if (tWhile.getWait() != null) {
 	    return tWhile.getWait();
 	} else if (tWhile.getThrow() != null) {
 	    return tWhile.getThrow();
 	} else if (tWhile.getTerminate() != null) {
 	    return tWhile.getTerminate();
 	} else if (tWhile.getFlow() != null) {
 	    return tWhile.getTerminate();
 	} else if (tWhile.getSwitch() != null) {
 	    return tWhile.getSwitch();
 	} else if (tWhile.getWhile() != null) {
 	    return tWhile.getWhile();
 	} else if (tWhile.getPick() != null) {
 	    return tWhile.getWhile();
 	} else if (tWhile.getScope() != null) {
 	    return tWhile.getScope();
 	} else {
 	    return null;
 	}
     }
 
     private TExtensibleElements obterElementoFilho(TOnMessage tOnMessage) {
 	if (tOnMessage.getSequence() != null) {
 	    return tOnMessage.getSequence();
 	} else if (tOnMessage.getEmpty() != null) {
 	    return tOnMessage.getEmpty();
 	} else if (tOnMessage.getInvoke() != null) {
 	    return tOnMessage.getInvoke();
 	} else if (tOnMessage.getReceive() != null) {
 	    return tOnMessage.getReceive();
 	} else if (tOnMessage.getReply() != null) {
 	    return tOnMessage.getReply();
 	} else if (tOnMessage.getAssign() != null) {
 	    return tOnMessage.getAssign();
 	} else if (tOnMessage.getWait() != null) {
 	    return tOnMessage.getWait();
 	} else if (tOnMessage.getThrow() != null) {
 	    return tOnMessage.getThrow();
 	} else if (tOnMessage.getTerminate() != null) {
 	    return tOnMessage.getTerminate();
 	} else if (tOnMessage.getFlow() != null) {
 	    return tOnMessage.getTerminate();
 	} else if (tOnMessage.getSwitch() != null) {
 	    return tOnMessage.getSwitch();
 	} else if (tOnMessage.getWhile() != null) {
 	    return tOnMessage.getWhile();
 	} else if (tOnMessage.getPick() != null) {
 	    return tOnMessage.getWhile();
 	} else if (tOnMessage.getScope() != null) {
 	    return tOnMessage.getScope();
 	} else {
 	    return null;
 	}
     }
 
     private TExtensibleElements obterElementoFilho(TScope tScope) {
 	if (tScope.getSequence() != null) {
 	    return tScope.getSequence();
 	} else if (tScope.getEmpty() != null) {
 	    return tScope.getEmpty();
 	} else if (tScope.getInvoke() != null) {
 	    return tScope.getInvoke();
 	} else if (tScope.getReceive() != null) {
 	    return tScope.getReceive();
 	} else if (tScope.getReply() != null) {
 	    return tScope.getReply();
 	} else if (tScope.getAssign() != null) {
 	    return tScope.getAssign();
 	} else if (tScope.getWait() != null) {
 	    return tScope.getWait();
 	} else if (tScope.getThrow() != null) {
 	    return tScope.getThrow();
 	} else if (tScope.getTerminate() != null) {
 	    return tScope.getTerminate();
 	} else if (tScope.getFlow() != null) {
 	    return tScope.getTerminate();
 	} else if (tScope.getSwitch() != null) {
 	    return tScope.getSwitch();
 	} else if (tScope.getWhile() != null) {
 	    return tScope.getWhile();
 	} else if (tScope.getPick() != null) {
 	    return tScope.getWhile();
 	} else if (tScope.getScope() != null) {
 	    return tScope.getScope();
 	} else {
 	    return null;
 	}
     }
 
     private void refinarVerticesCompostos(Graph<Vertice, Arco> grafo,
 	    List<ProcessoNegocio> processosNegocio) {
 	Map<Integer, ProcessoNegocio> mapaPNs = new HashMap<Integer, ProcessoNegocio>();
 	for (ProcessoNegocio processoNegocio : processosNegocio) {
 	    mapaPNs.put(processoNegocio.getId(), processoNegocio);
 	}
 
 	Set<Vertice> verticesExcluidos = new HashSet<Vertice>();
 	Set<Arco> arcosExcluidos = new HashSet<Arco>();
 
 	for (Vertice vertice : grafo.getVertices()) {
 	    ProcessoNegocio pnRefinado = this.obterProcessoNegocioPorURL(
 		    processosNegocio, vertice
 			    .obterValorRotulo(Vertice.Rotulo.URL));
 	    if (pnRefinado != null) {
 		Collection<Vertice> verticesPNRefinado = this
 			.obterVerticesPNRefinado(grafo, pnRefinado.getId());
 
 		Set<ProcessoNegocio> pnsVertice = new HashSet<ProcessoNegocio>();
 		Set<Arco> arcosVertice = new HashSet<Arco>();
 		for (Arco arco : grafo.getInEdges(vertice)) {
 		    pnsVertice
 			    .add(mapaPNs
 				    .get(new Integer(
 					    arco
 						    .obterValorRotulo(Arco.Rotulo.ID_PROCESSO_NEGOCIO))));
 		    arcosVertice.add(arco);
 		}
 		for (Arco arco : grafo.getOutEdges(vertice)) {
 		    pnsVertice
 			    .add(mapaPNs
 				    .get(new Integer(
 					    arco
 						    .obterValorRotulo(Arco.Rotulo.ID_PROCESSO_NEGOCIO))));
 		    arcosVertice.add(arco);
 		}
 
 		Collection<Vertice> predecessores = grafo
 			.getPredecessors(vertice);
 		Collection<Vertice> sucessores = grafo.getSuccessors(vertice);
 
 		for (Arco arco : arcosVertice) {
 		    arcosExcluidos.add(arco);
 		}
 		verticesExcluidos.add(vertice);
 
 		Collection<Vertice> sucessoresVerticeInicio = obterSucessoresVerticeInicio(
 			grafo, pnRefinado.getId());
 		for (Vertice predecessor : predecessores) {
 		    for (Vertice sucessor : sucessoresVerticeInicio) {
 			for (ProcessoNegocio pnVertice : pnsVertice) {
 			    Map<Arco.Rotulo, String> rotulosArco = new HashMap<Arco.Rotulo, String>();
 			    rotulosArco.put(Arco.Rotulo.ID_PROCESSO_NEGOCIO,
 				    String.valueOf(pnVertice.getId()));
 			    rotulosArco.put(Arco.Rotulo.NUMERO_INVOCACOES, "0");
 			    Arco arco = new Arco(rotulosArco);
 			    grafo.addEdge(arco, predecessor, sucessor);
 			}
 		    }
 		}
 
 		Collection<Vertice> predecessoresVerticeFim = obterPredecessoresVerticeFim(
 			grafo, pnRefinado.getId());
 		for (Vertice sucessor : sucessores) {
 		    for (Vertice predecessor : predecessoresVerticeFim) {
 			for (ProcessoNegocio pnVertice : pnsVertice) {
 			    Map<Arco.Rotulo, String> rotulosArco = new HashMap<Arco.Rotulo, String>();
 			    rotulosArco.put(Arco.Rotulo.ID_PROCESSO_NEGOCIO,
 				    String.valueOf(pnVertice.getId()));
 			    rotulosArco.put(Arco.Rotulo.NUMERO_INVOCACOES, "0");
 			    Arco arco = new Arco(rotulosArco);
 			    grafo.addEdge(arco, predecessor, sucessor);
 			}
 		    }
 		}
 
 		for (ProcessoNegocio pn : pnsVertice) {
 		    for (Vertice verticePNRefinado : verticesPNRefinado) {
 			for (Vertice sucessor : grafo
 				.getSuccessors(verticePNRefinado)) {
 			    if (!(sucessor instanceof VerticeFim)) {
 				Map<Arco.Rotulo, String> rotulosArco = new HashMap<Arco.Rotulo, String>();
 				rotulosArco.put(
 					Arco.Rotulo.ID_PROCESSO_NEGOCIO, String
 						.valueOf(pn.getId()));
 				rotulosArco.put(Arco.Rotulo.NUMERO_INVOCACOES,
 					"0");
 				Arco arco = new Arco(rotulosArco);
 				grafo
 					.addEdge(arco, verticePNRefinado,
 						sucessor);
 			    }
 			}
 		    }
 		}
 	    }
 	}
 
 	for (Arco arco : arcosExcluidos) {
 	    grafo.removeEdge(arco);
 	}
 
 	for (Vertice vertice : verticesExcluidos) {
 	    grafo.removeVertex(vertice);
 	}
     }
 
     private ProcessoNegocio obterProcessoNegocioPorURL(
 	    List<ProcessoNegocio> processosNegocio, String url) {
 	for (ProcessoNegocio processoNegocio : processosNegocio) {
 	    if (processoNegocio.getURL().equals(url)) {
 		return processoNegocio;
 	    }
 	}
 	return null;
     }
 
     private Collection<Vertice> obterVerticesPNRefinado(
 	    Graph<Vertice, Arco> grafo, int idProcessoNegocio) {
 	Collection<Vertice> verticesPNRefinado = new HashSet<Vertice>();
 	for (Arco arco : grafo.getEdges()) {
 	    if (idProcessoNegocio == Integer.parseInt(arco
 		    .obterValorRotulo(Arco.Rotulo.ID_PROCESSO_NEGOCIO))) {
 		Vertice vertice = grafo.getDest(arco);
 		if (!(vertice instanceof VerticeInicio)
 			&& !(vertice instanceof VerticeFim)) {
 		    verticesPNRefinado.add(vertice);
 		}
 	    }
 	}
 	return verticesPNRefinado;
     }
 
     private Collection<Vertice> obterSucessoresVerticeInicio(
 	    Graph<Vertice, Arco> grafo, Integer idProcessoNegocio) {
 	Collection<Vertice> sucessoresVerticeInicio = new HashSet<Vertice>();
 	for (Vertice vertice : grafo.getVertices()) {
 	    if ((vertice instanceof VerticeInicio)
 		    && vertice.toString().contains(
 			    String.valueOf(idProcessoNegocio))) {
 		sucessoresVerticeInicio.addAll(grafo.getSuccessors(vertice));
 	    }
 	}
 	return sucessoresVerticeInicio;
     }
 
     private Collection<Vertice> obterPredecessoresVerticeFim(
 	    Graph<Vertice, Arco> grafo, Integer idProcessoNegocio) {
 	Collection<Vertice> predecessoresVerticeFim = new HashSet<Vertice>();
 	for (Vertice vertice : grafo.getVertices()) {
 	    if ((vertice instanceof VerticeFim)
 		    && vertice.toString().contains(
 			    String.valueOf(idProcessoNegocio))) {
 		predecessoresVerticeFim.addAll(grafo.getPredecessors(vertice));
 	    }
 	}
 	return predecessoresVerticeFim;
     }
 }

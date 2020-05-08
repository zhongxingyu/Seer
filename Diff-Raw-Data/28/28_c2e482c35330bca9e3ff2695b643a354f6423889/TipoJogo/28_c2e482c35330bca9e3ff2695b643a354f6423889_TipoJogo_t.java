 package br.ufpb.dce.bloomer.core.model;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;

 import javax.persistence.CascadeType;
 import javax.persistence.ElementCollection;
 import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.JsonNodeFactory;
 import org.codehaus.jackson.node.ObjectNode;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
 import org.springframework.roo.addon.json.RooJson;
 import org.springframework.roo.addon.tostring.RooToString;
 
 @RooJavaBean
 @RooToString
 @RooJpaActiveRecord
 @RooJson(deepSerialize = true)
 public class TipoJogo {
 
 	@NotNull
 	@Size(min = 2, max = 100)
 	private String nome;
 
 	@NotNull
 	@Size(max = 2000)
 	private String descricao;
 
	@ManyToOne
    private Usuario desenvolvedor;
	
 	@Enumerated
 	private Plafatorma plataforma;
 
 	@OneToMany(cascade = CascadeType.ALL, mappedBy = "tipo")
 	private Set<Jogo> jogos = new HashSet<Jogo>();
 
 	@ElementCollection
 	private Set<NivelTaxonomia> niveisTaxonomia = new HashSet<NivelTaxonomia>();
 
 	@OneToMany(cascade = CascadeType.ALL, mappedBy = "jogo")
 	private Set<TipoQuestao> questoes = new HashSet<TipoQuestao>();
 
 	public String toJson() {
 		ObjectNode noTipoJogo = tipojogo2json(this);
 		return noTipoJogo.toString();
 	}
 
 	public static String toJsonArray(Collection<TipoJogo> collection) {
 		ArrayNode arrayDeTipoJogos = JsonNodeFactory.instance.arrayNode();
 
 		for (TipoJogo tipojogo : collection) {
 			ObjectNode noTipoJogo = tipojogo2json(tipojogo);
 			arrayDeTipoJogos.add(noTipoJogo);
 		}
 
 		return arrayDeTipoJogos.toString();
 	}
 
 	private static ObjectNode tipojogo2json(TipoJogo tipojogo) {
 		ObjectNode noTipoJogo = JsonNodeFactory.instance.objectNode();
 
 		noTipoJogo.put("id", tipojogo.getId());
 		noTipoJogo.put("nome", tipojogo.getNome());
 		noTipoJogo.put("descricao", tipojogo.getDescricao());
		noTipoJogo.put("desenvolvedor", tipojogo.getDesenvolvedor().getId());
 		noTipoJogo.put("plataforma", tipojogo.getPlataforma().name());
 		noTipoJogo.put("niveisTaxonomia", tipojogo.getNiveisTaxonomia()
 				.toString());
 		noTipoJogo.put("version", tipojogo.getVersion());
 
 		return noTipoJogo;
 	}
 
 	public static TipoJogo fromJsonToTipoJogo(String json) {
 		ObjectMapper objectMapper = new ObjectMapper();
 		JsonFactory factory = objectMapper.getJsonFactory();
 		try {
 			JsonNode tipojogoJSON = objectMapper.readTree(factory
 					.createJsonParser(json));
 
 			TipoJogo tipojogo = new TipoJogo();
 
 			if (tipojogoJSON.has("id")) {
 				tipojogo.setId(tipojogoJSON.get("id").asLong());
 			}
 
 			if (tipojogoJSON.has("nome")) {
 				tipojogo.setNome(tipojogoJSON.get("nome").asText());
 			}
 
 			if (tipojogoJSON.has("descricao")) {
 				tipojogo.setDescricao(tipojogoJSON.get("descricao").asText());
 			}
 
			if (tipojogoJSON.has("desenvolvedor")) {
				Usuario conf = Usuario.findUsuario(tipojogoJSON.get("desenvolvedor").asLong());
				tipojogo.setDesenvolvedor(conf);
 			}
 
 			if (tipojogoJSON.has("plataforma")) {
 				tipojogo.setPlataforma(Plafatorma.valueOf(tipojogoJSON.get(
 						"plataforma").asText()));
 			}
 
 			if (tipojogoJSON.has("niveisTaxonomia")) {
 				Set<NivelTaxonomia> set = new HashSet<NivelTaxonomia>();
 				for (int i = 0; i < tipojogoJSON.get("niveisTaxonomia").size(); i++) {
 					set.add(NivelTaxonomia.valueOf(tipojogoJSON
 							.get("niveisTaxonomia").get(i).asText()));
 				}
 				tipojogo.setNiveisTaxonomia(set);
 			}
 
 			if (tipojogoJSON.has("version")) {
 				tipojogo.setVersion(tipojogoJSON.get("version").asInt());
 			}
 
 			return tipojogo;
 
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 }

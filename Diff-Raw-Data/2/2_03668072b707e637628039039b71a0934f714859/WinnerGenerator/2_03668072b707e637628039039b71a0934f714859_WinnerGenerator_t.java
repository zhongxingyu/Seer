 package nobugs.nolife.mw.generator;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.TypedQuery;
 
 import nobugs.nolife.mw.MWException;
 import nobugs.nolife.mw.entities.Material;
 import nobugs.nolife.mw.entities.Memento;
 import nobugs.nolife.mw.entities.TaggedMaterial;
 import nobugs.nolife.mw.util.Constants;
 import nobugs.nolife.mw.util.MaterialUtil;
 import nobugs.nolife.mw.util.PathUtil;
 
 public class WinnerGenerator extends Generator {
 	@Override
 	protected String affectedMemento(Material m, String tag) {
 		return getMementoId(m,tag)+".html";
 	}
 
 	/** TaggedMaterial tmƓꃁgɑTaggedMaterial邽߂TypedQueryԋp */
 	@Override
 	protected TypedQuery<TaggedMaterial> queryBelongingSameMementoWith(TaggedMaterial tm) {
 		Material m = tm.getMaterial();
 		TypedQuery<TaggedMaterial> query = em.createQuery(
 				"SELECT tm FROM Material m , m.taggedMaterials tm " +
 						"WHERE tm.id.tag = 'winner' AND tm.id.materialId like :yyyy AND tm.tagState <> :tagState",TaggedMaterial.class);
		query.setParameter("yyyy", MaterialUtil.getMaterialYear(m)+"%");
 		query.setParameter("tagState", Constants.TAG_STATE_NOT_IN_USE);
 		return query;
 	}
 
 	@Override
 	protected Memento generateMemento(List<TaggedMaterial> updateTargetList) throws MWException {
 
 		// 擪1Material
 		Material m = updateTargetList.get(0).getMaterial();
 		String year = MaterialUtil.getMaterialYear(m);
 		String month = MaterialUtil.getMaterialMonth(m);
 		String outfile = PathUtil.getDirectoryProperty(Constants.DIRPROP_KEY_MW_WINNER)+"\\"+affectedMemento(m,null);
 
 		// velocityp̃}bv
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("g",this);
 		map.put("list",updateTargetList);
 		map.put("year",year);
 		map.put("month",month);
 		map.put("title","qȐgl("+year+"N̏gl)");
 
 		TemplateWrapper tw = new TemplateWrapper();
 		tw.setContext(map);
 		tw.setTemplate("nobugs/nolife/mw/generator/template/winnerPage.html");
 		tw.setLevel(1);
 		tw.setOutput(outfile);
 		tw.out();
 
 		// ԋppg𐶐
 		Memento memento = new Memento();
 		memento.setMementoId(getMementoId(m));
 		memento.setCategory("winnerPage");
 		memento.setProductionPath(outfile);
 		 // mapped by mementos(TaggedMaterial)Ȃ̂ŁATaggedMaterialmementosݒ
 		for(TaggedMaterial tm:updateTargetList) {
 			tm.getMementos().add(memento);
 		}
 		memento.setTaggedMaterials(updateTargetList);
 
 		return memento;
 	}
 
 	@Override
 	protected String getMementoId(Material m, String tag) {
 		return getMementoId(m);
 	}
 
 	private String getMementoId(Material m) {
 		return "w_"+MaterialUtil.getMaterialYearMonth(m); // winneȑꍇw_yyyy
 	}
 
 }

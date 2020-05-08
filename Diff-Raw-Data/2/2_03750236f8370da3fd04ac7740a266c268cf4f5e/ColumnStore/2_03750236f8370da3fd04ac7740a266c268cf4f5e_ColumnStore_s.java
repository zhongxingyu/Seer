 package varviewer.client.varTable;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import varviewer.client.IGVInterface;
 import varviewer.shared.Variant;
 
 import com.google.gwt.cell.client.ButtonCell;
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.cell.client.ImageResourceCell;
 import com.google.gwt.cell.client.SafeHtmlCell;
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.ui.Image;
 
 /**
  * Maintains a list of all available Columns that can potentially be used in a ColumnModel
  * and VarTable. 
  * This is a singleton.
  * @author brendan
  *
  */
 public class ColumnStore {
 
 	private static List<VarAnnotation<?>> cols = new ArrayList<VarAnnotation<?>>();
 	
 	private static ColumnStore store;
 	
 	public static ColumnStore getStore() {
 		if (store == null) {
 			store = new ColumnStore();
 		}
 		
 		return store;
 	}
 	
 	/**
 	 * Private constructor, get access to the store statically through ColumnStore.getStore()
 	 */
 	private ColumnStore() {
 		initialize();
 		store = this;
 	}
 	
 	/**
 	 * Obtain the column associated with the given key
 	 * @param key
 	 * @return
 	 */
 	public VarAnnotation<?> getColumnForID(String key) {
 		for(VarAnnotation<?> col : cols) {
 			if (col.id.equals(key)) {
 				return col;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Obtain a reference to a list of all potential columns
 	 * @return
 	 */
 	public List<VarAnnotation<?>> getAllColumns() {
 		return cols;
 	}
 	
 	private void addColumn(VarAnnotation<?> col) {
 		cols.add(col);
 	}
 	
 	/**
 	 * Creates all possible columns and stores them in a list here....
 	 */
 	private void initialize() {
 		addColumn(new VarAnnotation<String>("gene", "Gene", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				String val = var.getAnnotationStr("gene");
 				return val != null ? val : "-";
 			}
 		}, 1.0));
 
 		VarAnnotation<String> chrAnno = new VarAnnotation<String>("contig", "Chr", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				return var.getChrom();
 			}
 		}, 0.5); 
 		chrAnno.setComparator(new Comparator<Variant>() {
 
 			@Override
 			public int compare(Variant o1, Variant o2) {
 				return o1.getChrom().compareTo(o2.getChrom());
 			}
 			
 		});
 		addColumn(chrAnno);
 
 		VarAnnotation<String> posAnnotation = new VarAnnotation<String>("pos", "Start", new TextColumn<Variant>() {
 			@Override
 			public String getValue(Variant var) {
 				return "" + var.getPos();
 			}
 		}, 1.0, new PositionComparator()); 
 		
 		posAnnotation.setComparator(new Comparator<Variant>() {
 
 			@Override
 			public int compare(Variant v0, Variant v1) {
 				if (v0.getChrom().equals(v1.getChrom())) {
 					return v1.getPos() - v0.getPos();
 				}
 				else {
 					return v0.getChrom().compareTo(v1.getChrom());
 				}
 			}
 			
 		});
 		addColumn(posAnnotation);
 		
 //		addColumn(new VarAnnotation<String>("zygosity", "Zygosity", new TextColumn<Variant>() {
 //
 //			@Override
 //			public String getValue(Variant var) {
 //				String val = var.getAnnotation("zygosity");
 //			
 //				if (val == null || val.length()<2)
 //					return "?";
 //					
 //				return val;
 //			}
 //		}, 1.0, false));
 		
 		addColumn(new VarAnnotation<ImageResource>("zygosity", "Zygosity", new Column<Variant, ImageResource>(new ImageResourceCell()) {
 
 			@Override
 			public ImageResource getValue(Variant var) {
 				String zyg = var.getAnnotationStr("zygosity");
 								
 				if (zyg == null || zyg.equals("ref"))
 					return resources.refImage();
 				if (zyg.equals("het"))
 					return resources.hetImage();
 				if (zyg.equals("hom"))
 					return resources.homImage();
 				
 				return resources.refImage();
 			}
 			
 		}, 0.6));
 
 
 		addColumn(new VarAnnotation<String>("exon.function", "Exon effect", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				String val = var.getAnnotationStr("exon.function");
 				if (val == null || val.equals("-")) {
 					val = var.getAnnotationStr("variant.type");
 				}
 				return val != null ? val : "-";
 			}
 		}, 2.0));
 
 		addColumn(new VarAnnotation<String>("nm.number", "NM Number", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				String val = var.getAnnotationStr("nm.number");
 				return val != null ? val : "-";
 			}
 		}, 2.0));
 
 		addColumn(new VarAnnotation<String>("cdot", "c.dot", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				String val = var.getAnnotationStr("cdot");
 				return val != null ? val : "-";
 			}
 		}, 2.0));
 
 		addColumn(new VarAnnotation<String>("pdot", "p.dot", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				String val = var.getAnnotationStr("pdot");
 				return val != null ? val : "-";
 			}
 		}, 2.0));
 
 		addColumn(new VarAnnotation<String>("ref", "Ref.", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				return var.getRef();
 			}
 		}, 1.0));
 		
 		addColumn(new VarAnnotation<String>("alt", "Alt.", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				return var.getAlt();
 			}
 		}, 1.0));
 		
 		addColumn(new VarAnnotation<String>("quality", "Quality", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				Double val = var.getAnnotationDouble("quality");
 				return val != null ? val.toString() : "-";
 			}
 		}, 1.0));
 
 		addColumn(new VarAnnotation<String>("depth", "Depth", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				Double val = var.getAnnotationDouble("depth");
 				return val != null ? val.toString() : "-";
 			}
 		}, 1.0));
 		
 		addColumn(new VarAnnotation<String>("var.freq", "Alt. Freq", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				Double tot = var.getAnnotationDouble("depth");
 				Double alt = var.getAnnotationDouble("var.depth");
 				if (tot != null && alt != null && tot > 0) {
 					
 						double freq = alt / tot;
 						String freqStr = "" + freq;
 						if (freqStr.length() > 5) {
 							freqStr = freqStr.substring(0, 4);
 						}
 						return freqStr;
 					
 					
 				}
 				return "-";
 			}
 		}, 1.0));
 		
 		addColumn(new VarAnnotation<SafeHtml>("varbin.bin", "VarBin", new Column<Variant, SafeHtml>(new SafeHtmlCell()) {
 
 			@Override
 			public SafeHtml getValue(Variant var) {
 				SafeHtmlBuilder bldr = new SafeHtmlBuilder();
 				Double val = var.getAnnotationDouble("varbin.bin");
 				
 				if (val == null) {
 					bldr.appendEscaped("-");
 				}
 				else {
 					if (val.equals(1)) {
 						bldr.appendHtmlConstant("<span style=\"color: #003300;\"><b>1</b></span>");	
 					}
 					if (val.equals(2)) {
 						bldr.appendHtmlConstant("<span style=\"color: #996600;\"><b>2</b></span>");	
 					}
 					if (val.equals(3)) {
 						bldr.appendHtmlConstant("<span style=\"color: #990000;\"><b>3</b></span>");	
 					}
 					if (val.equals(4)) {
 						bldr.appendHtmlConstant("<span style=\"color: #FF0000;\"><b>4</b></span>");	
 					}
 					
 				}
 				return bldr.toSafeHtml();
 			}
 			
 		}, 1.0));
 		
 		addColumn(new VarAnnotation<String>("pop.freq", "Pop. Freq.", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				Double val = var.getAnnotationDouble("pop.freq");
 				return val != null ? val.toString() : "0";
 			}
 		}, 1.0));
 		
 		addColumn(new VarAnnotation<String>("arup.freq", "ARUP Freq.", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				String val = var.getAnnotationStr("ARUP.freq");
 				if (val.equals("-"))
 					val = "0";
 				return val != null ? val : "0";
 			}
 		}, 2.0));
 
 		
 		addColumn(new VarAnnotation<String>("sift.score", "SIFT score", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				Double val = var.getAnnotationDouble("sift.score");
 				return val != null ? val.toString() : "NA";
 			}
 		}, 1.0));
 		
 		addColumn(new VarAnnotation<String>("mt.score", "MutationTaster score", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				Double val = var.getAnnotationDouble("mt.score");
 				return val != null ? val.toString() : "NA";
 			}
 		}, 1.0));
 		
 		addColumn(new VarAnnotation<String>("gerp.score", "GERP++ score", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				Double val = var.getAnnotationDouble("gerp.score");
 				return val != null ? val.toString() : "NA";
 			}
 		}, 1.0));
 		
 		addColumn(new VarAnnotation<SafeHtml>("rsnum", "dbSNP #", new Column<Variant, SafeHtml>(new SafeHtmlCell()) {
 
 			@Override
 			public SafeHtml getValue(Variant var) {
 				SafeHtmlBuilder bldr = new SafeHtmlBuilder();
 				String val = var.getAnnotationStr("rsnum");
 				if (val == null || val.length() < 2) {
 					bldr.appendEscaped("-");
 				}
 				else {
					bldr.appendHtmlConstant("<a href=\"http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=1063736\" target=\"_blank\">" + val + "</a>" );
 				}
 				return bldr.toSafeHtml();
 			}
 			
 		}, 1.0));
 		
 		addColumn(new VarAnnotation<String>("pp.score", "PolyPhen-2 score", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				Double val = var.getAnnotationDouble("pp.score");
 				return val != null ? val.toString() : "NA";
 			}
 		}, 1.0));
 		
 		addColumn(new VarAnnotation<String>("omim.num", "OMIM #", new TextColumn<Variant>() {
 
 			@Override
 			public String getValue(Variant var) {
 				String val = var.getAnnotationStr("omim.disease.ids");
 				return val != null ? val : "0";
 			}
 		}, 2.0));
 		
 		addColumn(new VarAnnotation<ImageResource>("disease.pics", "HGMD & OMIM", new Column<Variant, ImageResource>(new ImageResourceCell()) {
 
 			@Override
 			public ImageResource getValue(Variant var) {
 				String hgmdExact = var.getAnnotationStr("hgmd.hit");
 				String hgmdGeneMatch = var.getAnnotationStr("hgmd.info");
 				String omimGeneMatch = var.getAnnotationStr("omim.disease");
 				
 				boolean hasHGMDExact = hgmdExact != null && hgmdExact.length() > 3;
 				boolean hasHGMDGene = hgmdGeneMatch != null && hgmdGeneMatch.length() > 3;
 				boolean hasOmim = omimGeneMatch != null && omimGeneMatch.length() > 3;
 				
 				ImageResource img = null;
 				if (hasHGMDExact) {
 					if (hasOmim) {
 						//Has all 3
 						img = resources.hgmdHitHgmdOmimImage();
 					}
 					else {
 						//No omim, but has hgmd exact and gene match
 						img = resources.hgmdHitHgmdImage();
 					}
 				}
 				else {
 					//No exact hit
 					if (hasHGMDGene) {
 						if (hasOmim) {
 							img = resources.hgmdOmimImage();
 						}
 						else {
 							//No OMIM, just hgmd gene match
 							img = resources.hgmdOnlyImage();
 						}
 					}
 					else {
 						if (hasOmim) {
 							img = resources.omimOnlyImage();
 						}
 						else {
 							//nothing, img is null
 						}
 					}
 				}
 
 				return img;
 			}
 			
 		}, 1.0, null));
 	
 //		ButtonImageCell commentButton = new ButtonImageCell(new Image("images/comment-icon.png"));
 //		VarAnnotation<String> commentVarAnno =new VarAnnotation<String>("comment", "Notes", new Column<Variant, String>(commentButton) {
 //
 //			@Override
 //			public String getValue(Variant var) {
 //				//Somehow get comment info from Variant - is it an annotation?
 //				
 //				return "huh?";
 //			}
 //			
 //		}, 0.6);
 //		
 //		commentVarAnno.col.setFieldUpdater(new FieldUpdater<Variant, String>() {
 //
 //			@Override
 //			public void update(int index, Variant var, String value) {
 //				//TODO Show comment popup? 
 //			}
 //			
 //		});
 //		addColumn(commentVarAnno);
 		
 		
 		IGVCell igvCell = new IGVCell();
 		
 		addColumn(new VarAnnotation<String>("igv.link", "IGV", new Column<Variant, String>(igvCell) {
 
 			@Override
 			public String getValue(Variant var) {
 				String locus = "chr" + var.getChrom() + ":" + var.getPos();
 				return locus;
 			}
 			
 		}, 1.0, null));
 			
 	}
 	
 	public class ButtonImageCell extends ButtonCell{
 
 		final Image image;
 		
 		public ButtonImageCell(Image image) {
 			this.image = image;
 		}
 		
 	    @Override
 	    public void render(com.google.gwt.cell.client.Cell.Context context, 
 	            String value, SafeHtmlBuilder sb) {
 	        SafeHtml html = SafeHtmlUtils.fromTrustedString(image.toString());
 	        sb.append(html);
 	    }
 	}
 	
 	static class IGVCell extends ButtonCell {
 		
 		public void render(Cell.Context context, String value, SafeHtmlBuilder sb) {
 			sb.appendHtmlConstant("<a href=\"" + IGVInterface.baseURL + "goto?locus=" + value + "\" target=\"_self\"><img src=\"images/linkIcon.png\"/></a>" );
 		}
 	}
 	
 	static final VarPageResources resources = (VarPageResources) GWT.create(VarPageResources.class);
 	
 	
 }

 package net.derkholm.nmica.extra.app;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.derkholm.nmica.model.motif.NMWeightMatrix;
 import net.derkholm.nmica.motif.Motif;
 import net.derkholm.nmica.motif.MotifComparitorIFace;
 import net.derkholm.nmica.motif.MotifIOTools;
 import net.derkholm.nmica.motif.SquaredDifferenceMotifComparitor;
 import net.derkholm.nmica.motif.align.MotifAlignment;
 
 import org.biojava.bio.dist.Distribution;
 import org.biojava.bio.dist.SimpleDistribution;
 import org.biojava.bio.dp.WeightMatrix;
 import org.biojava.bio.symbol.FiniteAlphabet;
 import org.biojava.bio.symbol.IllegalAlphabetException;
 import org.biojava.bio.symbol.IllegalSymbolException;
 import org.biojava.bio.symbol.Symbol;
 import org.biojava.utils.ChangeVetoException;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 @App(overview = "A tool for outputting subsets of motif sets based on name lists or a regular expression", generateStub = true)
 @NMExtraApp(launchName = "nmgrep", vm = VirtualMachine.SERVER)
 public class MotifGrep {
 
 	private File motifs;
 	private File list;
 	private Pattern pattern;
 	private String[] names;
 	private String replaceWithStr = null;
 	private boolean substring;
 	private String prefix;
 	private String annotationKey;
 	private boolean matchSpecies;
 	private boolean matchDesc;
 	private boolean matchName = true;
 	private int stripColumnsFromLeft;
 	private int stripColumnsFromRight;
 	private int[] indices;
 	private int[] ignoreIndices;
 	private boolean aligned;
 	private MotifComparitorIFace motifComparitor = SquaredDifferenceMotifComparitor.getMotifComparitor();
 	private boolean printFilename;
 
 	private HashMap<Motif, File> motifToFilenameMap = new HashMap<Motif, File>();
 	private boolean printMotifName;
 	private File[] motifFiles;
 	private String[] values;
 	private String[] keys;
 	private boolean override;
 	private PrintStream outputStream = System.out;
 	private boolean outputAll = true;
 	private String[] removedKeys;
 	
 	@Option(help = "List of motif names to match against. "
 			+ "Note that this is done by exact comparison, "
 			+ "not by finding matching substrings", optional = true)
 	public void setList(File list) {
 		this.list = list;
 	}
 
 	@Option(help = "Strip specified number of columns from the left", optional = true)
 	public void setStripColumnsFromLeft(int i) {
 		stripColumnsFromLeft = i;
 	}
 
 	@Option(help = "Strip specified number of columns from the right", optional = true)
 	public void setStripColumnsFromRight(int i) {
 		stripColumnsFromRight = i;
 	}
 	
 	@Option(help = "Extract motifs at indices (first index : 0)", optional = true)
 	public void setAtIndex(int[] inds) {
 		indices = inds;
 	}
 	
 	@Option(help = "Ignore motifs at indices (first index : 0)", optional = true)
 	public void setIgnoreAtIndex(int[] inds) {
 		ignoreIndices = inds;
 	}
 
 	@Option(help = "Allowed motif names (separated by spaces)", optional = true)
 	public void setNames(String[] names) {
 		this.names = names;
 	}
 	
 	@Option(help = "Find motifs whose name contains the specified string as a substring, "
 			+ "rather than looking for exact matches "
 			+ "(this switch works with -list and -names, default=false)", optional = true)
 	public void setMatchSubstring(boolean b) {
 		this.substring = b;
 	}
 
 	@Option(help = "Regular expression to match against", optional = true)
 	public void setExp(String str) {
 		this.pattern = Pattern.compile(str);
 	}
 
 	@Option(help = "Include the motif name in regular expression matching (default=true)", optional = true)
 	public void setMatchName(boolean bool) {
 		this.matchName = bool;
 	}
 
 	@Option(help = "Include the species name in regular expression matching (default=false)", optional = true)
 	public void setMatchSpecies(boolean bool) {
 		this.matchSpecies = bool;
 	}
 
 	@Option(help = "Include the description annotations in regular expression matching (default=false)", optional = true)
 	public void setMatchDesc(boolean bool) {
 		this.matchDesc = bool;
 	}
 
 	@Option(help = "Print out the value for the specified annotation key", optional = true)
 	public void setAnnotation(String str) {
 		this.annotationKey = str;
 	}
 
 	@Option(help = "Replacement string for the regular expression specified with -exp "
 			+ "(replaces all instances)", optional = true)
 	public void setReplaceWith(String str) {
 		this.replaceWithStr = str;
 	}
 
 	@Option(help = "Add prefix to the motif names", optional = true)
 	public void setAddPrefix(String str) {
 		this.prefix = str;
 	}
 
 	@Option(help = "Input motifset file(s)")
 	public void setMotifs(File[] motifs) {
 		this.motifFiles = motifs;
 	}
 
 	@Option(help = "Output aligned motifs (default = false)",optional=true)
 	public void setAligned(boolean b) {
 		this.aligned = b;
 	}
 	
 	@Option(help = "Print the matching filename instead of the motif", optional = true)
 	public void setPrintFilename(boolean b) {
 		this.printFilename = b;
 	}
 	
 	@Option(help = "Print the matching motif name instead of the moti", optional = true)
 	public void setPrintMotifname(boolean b) {
 		this.printMotifName = b;
 	}
 	
 	@Option(help = "Assign the specified value to annotation(s) specified with -annotation", optional=true)
 	public void setAssignValue(String[] values) {
 		this.values = values;
 	}
 	
 	@Option(help = "Assign values to specified annotation key(s)", optional=true)
 	public void setAssignKey(String[] keys) {
 		this.keys = keys;
 	}
 	
 	@Option(help = "Allow overriding existing annotation values (default=false)", optional=true)
 	public void setOverride(boolean b) {
 		this.override = b;
 	}
 	
 	@Option(help = 
 			"Output all motifs rather than just the ones that matched the pattern (default=true). " +
 			"Only applies when -assignKey and -assignValue were used, or when -removeKeys was used.", optional = true)
 	public void setOutputAll(boolean b) {
 		this.outputAll = b;
 	}
 	
 	@Option(help = "Remove the annotation(s) with the specified key(s)", optional=true)
 	public void setRemoveKey(String[] keys) {
 		this.removedKeys = keys;
 	}
 	
 	@Option(help = "Output file", optional=true)
 	public void setOut(File f) throws FileNotFoundException {
 		this.outputStream = new PrintStream(f);
 	}
 
 	
 	/**
 	 * @param args
 	 */
 	public void main(String[] args) throws Exception {
 		Motif[] motifs;
 		List<Motif> motifList = new ArrayList<Motif>();
 		for (File f : motifFiles) {
 			FileReader fr = new FileReader(f);
 			Motif[] ms = MotifIOTools.loadMotifSetXML(fr);
 			motifList.addAll(Arrays.asList(ms));
 			for (Motif m : ms) motifToFilenameMap.put(m, f);
 			
 			fr.close();
 		}
 		motifs = motifList.toArray(new Motif[0]);
 		
 		List<Motif> om = new ArrayList<Motif>();
 
 		if (names != null && list != null) {
 			System.err.println("Supply only either -names or -list");
 			System.exit(1);
 		}
 
 		if (names != null) {
 			for (String str : names) {
 				for (Motif m : motifs) {
 					
 					if (substring ? m.getName().contains(str) : m.getName()
 							.equals(str)) {
 						om.add(m);
 						break;
 					}
 				}
 			}
 		} else if (list != null) {
 			BufferedReader br = new BufferedReader(new FileReader(list));
 			for (String line = br.readLine(); line != null; line = br
 					.readLine()) {
 				for (Motif m : motifs) {
 					if (substring ? m.getName().contains(line) : m.getName()
 							.equals(line)) {
 						om.add(m);
 						break;
 					}
 				}
 			}
 		} else if (pattern != null) {
 			for (Motif m : motifs) {
 				if (replaceWithStr != null) {
 					Matcher matcher = pattern.matcher(m.getName());
 					if (matcher.find()) {
						System.err.printf("Found a match to pattern %s. Will replace with %s%n",pattern.toString(),replaceWithStr);
 						m.setName(matcher.replaceAll(replaceWithStr));
 					}
 					om.add(m);
 				} else {
 					if (matchName && (pattern.matcher(m.getName()).find())) {
 						om.add(m);
 						continue;
 					}
 					if (matchDesc
 							&& m.getAnnotation()
 									.containsProperty("description")
 							&& (pattern
 									.matcher((CharSequence) m.getAnnotation()
 											.getProperty("description")).find())) {
 						om.add(m);
 						continue;
 					}
 					if (matchSpecies
 							&& m.getAnnotation().containsProperty("species")
 							&& (pattern.matcher((CharSequence) m
 									.getAnnotation().getProperty("species"))
 									.find())) {
 						om.add(m);
 						continue;
 					}
 				}
 			}
 		} else if (indices != null) {
 			for (int i : indices) {
 				om.add(motifs[i]);
 			}
 		}
 		else if (ignoreIndices != null) {
 			Arrays.sort(ignoreIndices);
 			for (int i = 0; i < motifs.length; i++) {
 				
 				//if the index isn't found from the ignored indices list, add it to output set
 				if (Arrays.binarySearch(ignoreIndices, i) < 0) {
 					om.add(motifs[i]);
 				}
 			}
 		} else { // just add every motif from input to the output set
 			for (Motif m : motifs)
 				om.add(m);
 		}
 
 		if (prefix != null) {
 			for (Motif m : om) {
 				m.setName(prefix + m.getName());
 			}
 		}
 
 		if (annotationKey != null) {
 			for (Motif m : om) {
 				if (m.getAnnotation().containsProperty(annotationKey)) {
 					outputStream.printf("%s\t%s\t%s%n", m.getName(),
 							annotationKey, m.getAnnotation().getProperty(
 									annotationKey));
 				}
 			}
 		} else {
 			if (stripColumnsFromLeft > 0) {
 				for (int m = 0; m < om.size(); m++) {
 					Motif mot = om.get(m);
 					mot.setWeightMatrix(
 						stripColumnsFromLeft(
 								mot.getWeightMatrix(), stripColumnsFromLeft));
 				}
 			}
 
 			if (stripColumnsFromRight > 0) {
 				for (int m = 0; m < om.size(); m++) {
 					Motif mot = om.get(m);
 					mot.setWeightMatrix(
 						stripColumnsFromRight(
 								mot.getWeightMatrix(), stripColumnsFromRight));
 				}
 			}
 
 			if (this.keys != null) {
 				if (this.values == null) {
 					System.err.println("No annotation values to assign were specified.");
 					System.exit(1);
 				}
 				else if (this.values.length != this.keys.length) {
 					System.err.println(
 							"The number of specified annotation values " +
 							"does not match the number of specified annotation keys.");
 					System.exit(1);
 				}
 				
 				for (Motif m : om) {
 					for (int k = 0; k < keys.length; k++) {
 						String key = keys[k];
 						String val = values[k];
 						if (m.getAnnotation().containsProperty(key)) {
 							if (override) {
 								m.getAnnotation().setProperty(key, val);
 							} else {
 								System.err.printf(
 										"Annotation key '%s' is already set to value '%s' " +
 										"for motif with name %s. " +
 										"To override its value specify -override %n",
 										key,
 										m.getAnnotation().getProperty(key),
 										m.getName());
 							}							
 						} else {
 							m.getAnnotation().setProperty(key, val);
 						}
 					}
 				}
 			}
 			
 			if (removedKeys != null) {
 				for (Motif m : om) {
 					for (String key : removedKeys) {
 						if (m.getAnnotation().containsProperty(key)) {
 							System.err.printf("Removing property with key %s from motif %s%n", key,m.getName());
 							m.getAnnotation().removeProperty(key);
 						}
 					}
 				}
 			}
 			
 			if ((keys != null || removedKeys != null) && outputAll) {
 				System.err.println("Will output all motifs");
 				om = Arrays.asList(motifs);
 			}
 			
 			if (!printFilename &! printMotifName) {
 				
 				if (aligned) {
 					//TODO: Fix MotifAlignment so you don't need to do this three times to fix the offsets
 					MotifAlignment alignment = new MotifAlignment(om.toArray(new Motif[0]), motifComparitor);
 					alignment = new MotifAlignment(alignment.motifs(), motifComparitor);
 					alignment = new MotifAlignment(alignment.motifs(), motifComparitor);
 					alignment = alignment.alignmentWithZeroOffset();
 					om = Arrays.asList(alignment.motifs());
 				}
 				
 				MotifIOTools.writeMotifSetXML(
 						outputStream,
 						om.toArray(new Motif[0]));
 				
 				outputStream.flush();
 			}
 			
 			if (printFilename) {
 				//print motif file name plus name 
 				if (printMotifName) {
 					for (Motif m : om) {
 						File f = motifToFilenameMap.get(m);
 						outputStream.printf(
 							"%s\t%s%n",f.getPath(),m.getName());
 						outputStream.flush();
 					}					
 				} else {
 					//just print the matching filenames (once per file)
 					Set<File> files = new TreeSet<File>();
 					for (Motif m : om) {files.add(motifToFilenameMap.get(m));}
 					for (File f : files) {
 						outputStream.printf("%s%n",f.getPath());
 						outputStream.flush();
 					}
 				}
 			} 
 			else if (printMotifName) {
 				//just print the motif names (no filenames)
 				for (Motif m : om) {
 					outputStream.printf("%s%n",m.getName());
 					outputStream.flush();
 				}
 			}
 		}
 		
 		outputStream.flush();
 		outputStream.close();
 		
 	}
 
 	public WeightMatrix stripColumnsFromLeft(WeightMatrix inputWM, int count) throws IllegalAlphabetException, IllegalSymbolException, ChangeVetoException {
 		int colCount = inputWM.columns();
 		
 		if (colCount - count <= 0) {
 			throw new IllegalArgumentException(
 					"The input weight matrix hass too few columns.");
 		}
 		
 		Distribution[] dists = new Distribution[colCount - count];
 		for (int i = count; i < colCount; i++) {
 			Distribution d = inputWM.getColumn(i);
 			Distribution newD = new SimpleDistribution((FiniteAlphabet)d.getAlphabet());
 			
 			for (Iterator it = ((FiniteAlphabet) d.getAlphabet()).iterator(); it
 					.hasNext();) {
 				Symbol sym = (Symbol) it.next();
 				newD.setWeight(sym, d.getWeight(sym));
 			}
 			dists[i - count] = newD;
 			
 		}
 		
 		return new NMWeightMatrix(dists,colCount - count, 0);
 	}
 
 	public WeightMatrix stripColumnsFromRight(WeightMatrix inputWM, int count)
 			throws IllegalAlphabetException, IllegalSymbolException, ChangeVetoException {
 		int colCount = inputWM.columns();
 
 		if (colCount - count <= 0) {
 			throw new IllegalArgumentException(
 					"The input weight matrix hass too few columns.");
 		}
 
 		Distribution[] dists = new Distribution[colCount - count];
 
 		for (int i = 0; i < colCount - count; i++) {
 			Distribution d = inputWM.getColumn(i);
 			Distribution newD = new SimpleDistribution((FiniteAlphabet) d.getAlphabet());
 			
 			for (Iterator it = ((FiniteAlphabet) d.getAlphabet()).iterator(); it
 			.hasNext();) {
 				Symbol sym = (Symbol) it.next();
 				newD.setWeight(sym, d.getWeight(sym));
 			}
 
 			dists[i] = newD;
 		}
 
 		return new NMWeightMatrix(dists, inputWM.columns() - count, 0);
 	}
 }

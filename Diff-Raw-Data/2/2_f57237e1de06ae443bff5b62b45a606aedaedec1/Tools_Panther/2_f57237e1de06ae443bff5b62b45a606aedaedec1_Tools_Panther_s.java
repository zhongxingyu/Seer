 package enderdom.eddie.tools.bio;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.LinkedList;
 
 import org.apache.log4j.Logger;
 
 import enderdom.eddie.bio.homology.PantherGene;
 import enderdom.eddie.tools.Tools_String;
 
 public class Tools_Panther {
 	
 	private static String panthr = "pantherdb.org";
 	private static String search = "/webservices/garuda/search.jsp";
 
 	/**
 	 * 
 	 * @param pantherterm eg PTHR22751
 	 * @return list of genes attached to panther term as PantherGene objects
 	 * @throws EddieException
 	 * @throws URISyntaxException
 	 * @throws IOException
 	 */
 	public static PantherGene[] getGeneList(String pantherterm) throws EddieException, URISyntaxException, IOException{
 		//Check actually a panther acc
 		if(!pantherterm.startsWith("PTHR")){
 			Logger.getRootLogger().warn("Panther term does not start with PTHR, adding");
 			if(Tools_String.parseString2Int(pantherterm) == null){
 				throw new EddieException("Panther term "+pantherterm+"is not correct, should be like PTHR22751");
 			}
 			else{
 				pantherterm="PTHR"+pantherterm;
 			}
 		}
 		else Logger.getRootLogger().debug("Panther term is good");
 		//Get uRL 
		URI uri = new URI("http", panthr, search, "keyword=PTHR22751&listType=gene&type=getList", null);
 		URL site = uri.toURL();
 		//Read into buffer
 		Logger.getRootLogger().debug("Accessing Panther databse via http...");
 		BufferedReader in = new BufferedReader(new InputStreamReader(site.openStream()));
 
 		//Url shoudl have returned a column file such as:
 		/*
 			Gene Accession	Gene Name	Gene Symbol 
 			SCHMA|Gene=Smp_149580|UniProtKB=G4LXZ6	Peptide (FMRFamide/somatostatin)-like receptor,putative	G4LXZ6 
 			SCHMA|Gene=Smp_041700|UniProtKB=G4M0H4	Rhodopsin-like orphan GPCR,putative	G4M0H4 
 			SCHMA|Gene=Smp_161500|UniProtKB=G4LXX9	Rhodopsin-like orphan GPCR, putative	G4LXX9
 			... 
 		 */
 		//Skip Header
 		String inputLine;
 		LinkedList<PantherGene> genes =new LinkedList<PantherGene>();
 		Logger.getRootLogger().debug("Parsing Panther Gene list");
 		while ((inputLine = in.readLine()) != null){
 			if(inputLine.length() > 0 && !inputLine.startsWith("Gene Accession")){
 				PantherGene gene = new PantherGene();
 				String[] split = inputLine.split("\\|");
 				if(split.length != 3){
 					throw new EddieException("Line should be splittable with '|', line is not. (Line: " + inputLine+")");
 				}
 				gene.setShortSpecies(split[0]);
 				gene.setDbLink(split[0]);
 				String[] resplit = split[2].split("\\t");
 				if(split.length != 3){
 					throw new EddieException("Line should be splittable with '\\t', line is not. (Line: " + split[2]+")");
 				}
 				gene.setGeneAcc(resplit[0]);
 				gene.setGeneName(resplit[1]);
 				gene.setGeneSymbol(resplit[2]);
 				genes.add(gene);
 			}
 			System.out.print("\r"+ genes.size()+" genes from panther    ");
 		}
 		System.out.println();
 		Logger.getRootLogger().info(genes.size()+" Panther Gene objects retrieved");
 		in.close();
 		return genes.toArray(new PantherGene[0]);
 	}
 	
 	/**
 	 * 
 	 * @param longname species longname, must only be 2 words ie Xenopus tropicalis
 	 * @return the Panther short name species or null if the species is not available int the panther database
 	 * @throws EddieException
 	 * @throws URISyntaxException
 	 * @throws IOException 
 	 */
 	public static String getShortSpeciesName(String longname) throws EddieException, URISyntaxException, IOException{
 		URI uri = new URI("http", panthr, search, "type=organism", null);
 		URL site = uri.toURL();
 		BufferedReader in = new BufferedReader(new InputStreamReader(site.openStream()));
 		String inputLine;
 		while ((inputLine = in.readLine()) != null){
 			if(inputLine.length() > 0 && !inputLine.startsWith("Long Name")){
 				if(inputLine.toLowerCase().contains(longname.toLowerCase())){
 					String[] splits = inputLine.split("\\t");
 					return splits[splits.length-1];
 				}
 			}
 		}
 		return null;
 	}
 	
 }

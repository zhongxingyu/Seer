 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
  * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  * @author dmyersturnbull
  */
 package org.structnetalign.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.biojava.bio.structure.scop.ScopDatabase;
 import org.biojava.bio.structure.scop.ScopDomain;
 import org.biojava.bio.structure.scop.ScopFactory;
 
 /**
  * Uses the chain-level mapping from Andrew C.R. Martin at <a href="http://www.bioinf.org.uk/pdbsws/">the Martin Lab</a>.
  * See <a href="http://bioinformatics.oxfordjournals.org/content/21/23/4297.long">the paper</a> for more.
  * Perhaps the simplest mapping, but multi-chain domains are a problem.
  * @author dmyersturnbull
  *
  */
 public class MartinIdentifierMapping implements IdentifierMapping {
 
 	private Map<String,String> pdbIds;
 	private Map<String,Character> chainIds;
 
 	MartinIdentifierMapping() {
 		try {
 			pdbIds = new HashMap<String,String>();
 			chainIds = new HashMap<String,Character>();
 			File file = new File("src/main/resources/mappings/martin_pdb_uniprot_chain_map.lst");
 			BufferedReader br = new BufferedReader(new FileReader(file));
 			String line = "";
 			while ((line = br.readLine()) != null) {
 				if (line.isEmpty()) continue;
 				String[] parts = line.split("\\s+");
 				if (parts.length != 3 || line.contains("?")) {
 					continue;
 				}
 				if (pdbIds.containsKey(parts[2])) {
 					continue;
 				}
 				pdbIds.put(parts[2], parts[0]);
 				if (parts[1].length() != 1) continue;
 				chainIds.put(parts[2], parts[1].charAt(0));
 			}
 			br.close();
 		} catch (IOException e) {
 			throw new IllegalStateException("Couldn't initialize " + MartinIdentifierMapping.class.getSimpleName()); // fatal
 		}
 	}
 
 	@Override
 	public String uniProtToPdb(String uniProtId) {
		if (!pdbIds.containsKey(uniProtId) || !chainIds.containsKey(uniProtId)) return null;
 		return pdbIds.get(uniProtId) + "_" + chainIds.get(uniProtId);
 	}
 
 	@Override
 	public String uniProtToScop(String uniProtId) {
 		final String pdb = pdbIds.get(uniProtId);
 		if (chainIds.get(uniProtId) == null) return null;
 		final char chain = chainIds.get(uniProtId);
 		ScopDatabase scop = ScopFactory.getSCOP(ScopFactory.VERSION_1_75B);
 		List<ScopDomain> domains = scop.getDomainsForPDB(pdb);
 		for (ScopDomain domain : domains) {
 			List<String> ranges = domain.getRanges();
 			if (ranges.get(0).charAt(0) == chain) {
 				return domain.getScopId();
 			}
 		}
 		return null;
 	}
 
 	public int size() {
 		return chainIds.size();
 	}
 
 	public Map<String, String> getPdbIds() {
 		return pdbIds;
 	}
 
 	public Map<String, Character> getChainIds() {
 		return chainIds;
 	}
 
 }

 package ru.spbau.textminer.processor;
 import java.util.*;
 import java.io.*;
 
 public class ProcessChunkerOutput {
 	public static void main(String[] args) {
         if (args.length != 2) {
             System.out.println("Usage: java [-cp ...] ru.spbau.textminer.ProcessChunkerOutput input-file output-file");
             return;
         }
 
         BufferedReader reader = null;
         PrintWriter writer = null;
         try {
             reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "cp1251"));
             writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "cp1251"));
 
             String line;
             while ((line = reader.readLine()) != null) {
                 List<Chunk> chunks = getChunks(line);
 
                 int chunkNum = 0;
                 while (chunkNum < chunks.size()) {
                     Chunk chunk = chunks.get(chunkNum);
                     if (chunk.getType() == ChunkType.NP) {
                         List<Chunk> vpChunks = new ArrayList<Chunk>();
 
                         chunkNum++;
                         while (chunkNum < chunks.size() && chunks.get(chunkNum).getType() == ChunkType.VP) {
                             if (validVPChunk(chunks.get(chunkNum))) {
                                 vpChunks.add(chunks.get(chunkNum));
                             }
                             chunkNum++;
                         }
                         if (chunkNum < chunks.size() && vpChunks.size() == 1) {
                             if (validRelation(chunk, vpChunks.get(0), chunks.get(chunkNum))) {
                                 printRelation(writer, chunk, vpChunks, chunks.get(chunkNum));
                             }
                         }
                     } else {
                         chunkNum++;
                     }
                 }
             }
         } catch (IOException e) {
             System.out.println(e.getMessage());
         } finally {
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException ex) {}
             }
             if (writer != null) {
                 writer.close();
             }
         }
 
 	}
 
     private static List<Chunk> getChunks(String line) {
         List<String> strChunks = new ArrayList<String>();
         while (line.indexOf('[') != -1) {
             int i = line.indexOf('[');
             String rest = line.substring(i + 1);
             i = rest.indexOf(']');
             strChunks.add(rest.substring(0, i));
             line = rest.substring(i + 1);
         }
         List<Chunk> chunks = new ArrayList<Chunk>();
         for (String str : strChunks) {
             Chunk newChunk = new Chunk(str.startsWith("VP") ? ChunkType.VP : ChunkType.NP);
             String rest = str.substring(2);
             Scanner scanner = new Scanner(rest);
             while (scanner.hasNext()) {
                 String wordFeatures = scanner.next().trim();
                 int j = wordFeatures.indexOf('{');
                 String word = wordFeatures.substring(0, j);
                 String features = wordFeatures.substring(j + 1, wordFeatures.lastIndexOf('}'));
                 newChunk.getWords().add(word);
                 newChunk.getFeatures().add(features);
             }
             chunks.add(newChunk);
         }
         return chunks;
     }
 
 	private static boolean validRelation(Chunk leftChunk, Chunk vpChunk, Chunk rightChunk) {
 		if (!validLeftChunk(leftChunk) || !validVPChunk(vpChunk) || !validRightChunk(rightChunk)) {
 			return false;
 		}
 		boolean found = false;
 		for (String features : vpChunk.getFeatures()) {
 			if (getPOS(features) == POS.PR) {
 				found = true;
 			}
 		}
 		for (String features : rightChunk.getFeatures()) {
 			if (getPOS(features) == POS.PR) {
 				found = true;
 			}
 		}
 
 		return found && checkNumbers(leftChunk, vpChunk);
 	}
 
 	private static boolean checkNumbers(Chunk leftChunk, Chunk vpChunk) {
 		String nounFeatures = getFirstNounFeatures(leftChunk);
 		String verbFeatures = getFirstVerbFeatures(vpChunk);
 
 		if (nounFeatures.contains("ед")) {
 			return verbFeatures.contains("ед");
 		} else if (nounFeatures.contains("мн")) {
 			return verbFeatures.contains("мн");
 		}
 
 		return true;
 	}
 
 	private static String getFirstNounFeatures(Chunk chunk) {
 		for (int i = 0; i < chunk.getWords().size(); i++) {
 			POS pos = getPOS(chunk.getFeatures().get(i));
 			Case gc = getCase(chunk.getFeatures().get(i));
 
 			if ((pos == POS.S || pos == POS.NID) && (gc == Case.NOM)) {
 				return chunk.getFeatures().get(i);
 			}
 		}
 		return "";
 	}
 
 	private static String getFirstVerbFeatures(Chunk chunk) {
 		for (int i = 0; i < chunk.getWords().size(); i++) {
 			String features = chunk.getFeatures().get(i);
 			if (getPOS(features) == POS.V &&
 					!features.contains("деепр") && !features.contains("прич")) {
 				return chunk.getFeatures().get(i);
 			}
 		}
 		return "";
 	}
 
 	private static boolean validLeftChunk(Chunk chunk) {
 		boolean found = false;
 		for (int i = 0; i < chunk.getWords().size(); i++) {
 			POS pos = getPOS(chunk.getFeatures().get(i));
 			Case gc = getCase(chunk.getFeatures().get(i));
 
 			if ((pos == POS.S || pos == POS.NID) && (gc == Case.NOM)) {
 				found = true;
 			}
 		}
 		return found && (chunk.getWords().size() <= NP_CHUNK_SIZE);
 	}
 
 	private static boolean validRightChunk(Chunk chunk) {
 		boolean found = false;
 		for (int i = 0; i < chunk.getWords().size(); i++) {
 			POS pos = getPOS(chunk.getFeatures().get(i));
 
 			if ((pos == POS.S || pos == POS.NID)) {
 				found = true;
 			}
 		}
 		return found && (chunk.getWords().size() <= NP_CHUNK_SIZE);
 	}
 
 	private static boolean validVPChunk(Chunk chunk) {
 		boolean found = false;
 		for (int i = 0; i < chunk.getWords().size(); i++) {
 			String features = chunk.getFeatures().get(i);
 			if (getPOS(features) == POS.V &&
 					!features.contains("деепр") && !features.contains("прич")) {
 				found = true;
 			}
 		}
 		return found && chunk.getWords().size() <= VP_CHUNK_SIZE;
 	}
 
 	private static void printRelation(PrintWriter pw, Chunk leftChunk, List<Chunk> vpChunks, Chunk rightChunk) {
 		for (int i = 0; i < vpChunks.size(); i++) {
 			Chunk vpChunk = vpChunks.get(i);
 			pw.print(leftChunk);
 			pw.print(vpChunk);
 			pw.print(rightChunk);
 			pw.println();
 		}
 	}
 
 	private static POS getPOS(String features) {
		if (features.contains("ADVPRO")) {
 			return POS.ADV;
		} else if (features.contains("ADV")) {
 			return POS.ADV;
 		} else if (features.contains("ANUM")) {
 			return POS.NUM;
 		} else if (features.contains("APRO")) {
			return POS.S;
		} else if (features.contains("PART")) {
			return POS.PART;
 		} else if (features.contains("COM")) {
 			return POS.COM;
 		} else if (features.contains("CONJ")) {
 			return POS.CONJ;
 		} else if (features.contains("INTJ")) {
 			return POS.INTJ;
 		} else if (features.contains("NUM")) {
 			return POS.NUM;
		} else if (features.contains("A")) {
			return POS.A;
 		} else if (features.contains("SPRO")) {
 			return POS.S;
		} else if (features.contains("S")) {
			return POS.S;
		} else if (features.contains("PR")) {
			return POS.PR;
 		} else if (features.contains("V")) {
 			return POS.V;
 		} else {
 			return null;
 		}
 	}
 
 	private static Case getCase(String features) {
 		if (features.contains("им")) {
 			return Case.NOM;
 		} else if (features.contains("род")) {
 			return Case.GEN;
 		} else if (features.contains("дат")) {
 			return Case.DAT;
 		} else if (features.contains("вин")) {
 			return Case.ACC;
 		} else if (features.contains("твор")) {
 			return Case.INS;
 		} else if (features.contains("пр")) {
 			return Case.PREP;
 		} else if (features.contains("парт")) {
 			return Case.PART;
 		} else if (features.contains("местн")) {
 			return Case.LOC;
 		} else if (features.contains("зват")) {
 			return Case.VOC;
 		} else {
 			return null;
 		}
 	}
 
 	private static class Chunk {
         public Chunk(ChunkType type) {
             this.type = type;
             words = new ArrayList<String>();
             features = new ArrayList<String>();
         }
 
         public ChunkType getType() {
             return type;
         }
 
         public List<String> getWords() {
             return words;
         }
 
         public List<String> getFeatures() {
             return features;
         }
 
         public String toString() {
             StringBuilder sb = new StringBuilder();
             for (int i = 0; i < getWords().size(); i++) {
                 sb.append(getWords().get(i));
                 sb.append("{");
                 sb.append(getFeatures().get(i));
                 sb.append("}_");
                 sb.append(getPOS(getFeatures().get(i)));
                 sb.append(" ");
             }
             return sb.toString();
         }
 
 		private ChunkType type;
 		private List<String> words;
 		private List<String> features;
 	}
 
     private static enum ChunkType {
         NP, VP
     }
 
     private static enum Case {
         NOM, GEN, DAT, ACC, INS, PREP, PART, LOC, VOC
     }
 
     private static enum POS {
         A, ADV, NUM, S, COM, CONJ, INTJ, PART, PR, V , NID
     }
 
     private static final int NP_CHUNK_SIZE = 3;
     private static final int VP_CHUNK_SIZE = 5;
 }

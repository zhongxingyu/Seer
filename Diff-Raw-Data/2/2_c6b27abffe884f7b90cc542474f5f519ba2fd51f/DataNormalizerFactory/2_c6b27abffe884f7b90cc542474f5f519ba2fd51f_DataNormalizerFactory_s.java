 package cz.cuni.mff.odcleanstore.datanormalization;
 
 import cz.cuni.mff.odcleanstore.datanormalization.impl.DataNormalizerImpl;
 
 public class DataNormalizerFactory {
	public static DataNormalizer createNormalizator (Integer groupId) {
 		return new DataNormalizerImpl(groupId);
 	}
 }

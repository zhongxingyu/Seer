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
  * 
  * @author dmyersturnbull
  */
 package org.structnetalign.weight;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import org.apache.commons.math3.distribution.GammaDistribution;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.biojava3.alignment.FractionalIdentityScorer;
 import org.biojava3.alignment.NeedlemanWunsch;
 import org.biojava3.alignment.SimpleGapPenalty;
 import org.biojava3.alignment.SubstitutionMatrixHelper;
 import org.biojava3.alignment.template.GapPenalty;
 import org.biojava3.alignment.template.PairwiseSequenceScorer;
 import org.biojava3.alignment.template.SequencePair;
 import org.biojava3.alignment.template.SubstitutionMatrix;
 import org.biojava3.core.sequence.ProteinSequence;
 import org.biojava3.core.sequence.compound.AminoAcidCompound;
 import org.biojava3.core.sequence.io.FastaReaderHelper;
 import org.structnetalign.util.IdentifierMappingFactory;
 import org.structnetalign.util.MartinIdentifierMapping;
 
 public class NeedlemanWunschWeight implements AlignmentWeight {
 
 	/**
 	 * Scores with a gamma distribution as per Webber and Barton 2001, Bioinformatics. The authors used (among other
 	 * scoring schemes) the BLOSUM62 matrix with a gap opening penalty of 12 and extension penalty of 1. The paper is <a
 	 * href="http://bioinformatics.oxfordjournals.org/content/17/12/1158.full.pdf+html">available</a>.
 	 * 
 	 * @author dmyersturnbull
 	 * 
 	 */
 	private static class GammaScorer {
 		private double alpha = 25.54; // shape (normally theta)
 		private double beta = 4.96; // scale
 		private double lambda = 0.200;
 
 		public GammaScorer() {
 			super();
 		}
 
 		public GammaScorer(double alpha, double beta, double lambda) {
 			super();
 			this.alpha = alpha;
 			this.beta = beta;
 			this.lambda = lambda;
 		}
 
 		public double score(SequencePair<ProteinSequence, AminoAcidCompound> pair, double score) {
 			GammaDistribution dist = new GammaDistribution(alpha, lambda);
 			return dist.density(score + beta);
 		}
 
 	}
 
 	private static GapPenalty GAP_PENALTY = new SimpleGapPenalty((short) 12, (short) 1);
 
 	private static final Logger logger = LogManager.getLogger(NeedlemanWunschWeight.class.getName());
 
 	private static SubstitutionMatrix<AminoAcidCompound> MATRIX = SubstitutionMatrixHelper.getBlosum62();
 	private static final String URL = "http://www.uniprot.org/uniprot/%s.fasta";
 
	private ProteinSequence a;

	private ProteinSequence b;

 	private String uniProtId1;
 
 	private String uniProtId2;
 
 	int nSamples;
 
 	/**
 	 * @param args
 	 * @throws Exception
 	 */
 	@Deprecated
 	public static void main(String[] args) throws Exception {
 		if (args.length != 1) {
 			System.err.println("Usage: NeedlemanWunschWeight n-samples");
 			return;
 		}
 		int nSamples = Integer.parseInt(args[0]);
 		double lambda = run(nSamples);
 		System.out.println(lambda);
 	}
 
 	@Deprecated
 	public static double run(int nSamples) {
 		Random random = new Random();
 		MartinIdentifierMapping mapping = (MartinIdentifierMapping) IdentifierMappingFactory.getMapping();
 		Set<String> sdf = mapping.getChainIds().keySet();
 		List<String> ids = new ArrayList<String>(sdf.size());
 		ids.addAll(sdf);
 		int size = mapping.size();
 		logger.info("Running " + nSamples + " with population size " + size);
 		double total = 0;
 		for (int i = 0; i < nSamples; i++) {
 			String uniProtId1 = ids.get(random.nextInt(size));
 			String uniProtId2 = ids.get(random.nextInt(size));
 			ProteinSequence a, b;
 			try {
 				a = NeedlemanWunschWeight.getSequenceForId(uniProtId1);
 			} catch (Exception e) {
 				logger.error("Couldn't get sequence for " + uniProtId1, e);
 				i--;
 				continue;
 			}
 			try {
 				b = NeedlemanWunschWeight.getSequenceForId(uniProtId2);
 			} catch (Exception e) {
 				logger.error("Couldn't get sequence for " + uniProtId1, e);
 				i--;
 				continue;
 			}
 			NeedlemanWunsch<ProteinSequence, AminoAcidCompound> alg = new NeedlemanWunsch<>(a, b, GAP_PENALTY, MATRIX);
 			SequencePair<ProteinSequence, AminoAcidCompound> pair = alg.getPair();
 			PairwiseSequenceScorer<ProteinSequence, AminoAcidCompound> scorer = new FractionalIdentityScorer<>(pair);
 			double score = (double) scorer.getScore() / (double) scorer.getMaxScore();
 			logger.debug("Got score " + score + " for (" + uniProtId1 + "," + uniProtId2 + ")");
 			total += score;
 		}
 		return total / nSamples;
 	}
 
 	private static ProteinSequence getSequenceForId(String uniProtId) throws Exception {
 		try (InputStream stream = new URL(String.format(URL, uniProtId)).openStream()) {
 			return FastaReaderHelper.readFastaProteinSequence(stream).get(uniProtId); // why does this throw Exception?
 		}
 	}
 
 	@Override
 	public double assignWeight(String uniProtId1, String uniProtId2) throws Exception {
 		setIds(uniProtId1, uniProtId2);
 		return call().getWeight();
 	}
 
 	@Override
 	public WeightResult call() throws Exception {
 		ProteinSequence a = getSequenceForId(uniProtId1);
 		ProteinSequence b = getSequenceForId(uniProtId2);
 		NeedlemanWunsch<ProteinSequence, AminoAcidCompound> alg = new NeedlemanWunsch<>(a, b, GAP_PENALTY, MATRIX);
 		SequencePair<ProteinSequence, AminoAcidCompound> pair = alg.getPair();
 		PairwiseSequenceScorer<ProteinSequence, AminoAcidCompound> scorer = new FractionalIdentityScorer<>(pair);
 		double score = (double) scorer.getScore() / (double) scorer.getMaxScore();
 		GammaScorer gamma = new GammaScorer();
 		double prob = gamma.score(pair, score);
 		return new WeightResult(prob, uniProtId1, uniProtId2, this.getClass());
 	}
 
 	@Override
 	public void setIds(String uniProtId1, String uniProtId2) throws WeightException {
 		this.uniProtId1 = uniProtId1;
		try {
			a = getSequenceForId(uniProtId1);
		} catch (Exception e) {
			throw new WeightException("Could not get FASTA sequence for " + uniProtId1);
		}
 		this.uniProtId2 = uniProtId2;
		try {
			b = getSequenceForId(uniProtId2);
		} catch (Exception e) {
			throw new WeightException("Could not get FASTA sequence for " + uniProtId1);
		}
 	}
 }

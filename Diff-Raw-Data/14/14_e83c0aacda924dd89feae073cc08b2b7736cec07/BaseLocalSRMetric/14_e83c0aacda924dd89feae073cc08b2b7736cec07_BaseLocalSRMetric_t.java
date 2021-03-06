 package org.wikapidia.sr;
 
 import gnu.trove.set.TIntSet;
 import gnu.trove.set.hash.TIntHashSet;
 import org.wikapidia.core.WikapidiaException;
 import org.wikapidia.core.dao.DaoException;
 import org.wikapidia.core.dao.DaoFilter;
 import org.wikapidia.core.dao.LocalPageDao;
 import org.wikapidia.core.lang.Language;
 import org.wikapidia.core.lang.LanguageSet;
 import org.wikapidia.core.lang.LocalId;
 import org.wikapidia.core.lang.LocalString;
 import org.wikapidia.core.model.LocalPage;
 import org.wikapidia.matrix.SparseMatrix;
 import org.wikapidia.matrix.SparseMatrixRow;
 import org.wikapidia.sr.disambig.Disambiguator;
 import org.wikapidia.sr.normalize.IdentityNormalizer;
 import org.wikapidia.sr.normalize.Normalizer;
 import org.wikapidia.sr.pairwise.PairwiseSimilarity;
 import org.wikapidia.sr.pairwise.PairwiseSimilarityWriter;
 import org.wikapidia.sr.pairwise.SRFeatureMatrixWriter;
 import org.wikapidia.sr.utils.Dataset;
 import org.wikapidia.sr.utils.KnownSim;
 import org.wikapidia.sr.utils.Leaderboard;
 import org.wikapidia.utils.ParallelForEach;
 import org.wikapidia.utils.Procedure;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Logger;
 
 public abstract class BaseLocalSRMetric implements LocalSRMetric {
     private static Logger LOG = Logger.getLogger(BaseLocalSRMetric.class.getName());
     protected int numThreads = Runtime.getRuntime().availableProcessors();
     protected Disambiguator disambiguator;
     protected LocalPageDao pageHelper;
 
 
     private Normalizer defaultMostSimilarNormalizer = new IdentityNormalizer();
     private Normalizer defaultSimilarityNormalizer = new IdentityNormalizer();
     private Map<Integer, Normalizer> similarityNormalizers = new HashMap<Integer, Normalizer>();
     private Map<Integer, Normalizer> mostSimilarNormalizers = new HashMap<Integer, Normalizer>();
 
     protected Map<Language,SparseMatrix> mostSimilarLocalMatrices = new HashMap<Language, SparseMatrix>();
 
     @Override
     public void setMostSimilarLocalMatrices(Map<Language,SparseMatrix> mostSimilarLocalMatrices){
         this.mostSimilarLocalMatrices=mostSimilarLocalMatrices;
     }
 
     @Override
     public void setMostSimilarLocalMatrix(Language language, SparseMatrix sparseMatrix){
         this.mostSimilarLocalMatrices.put(language,sparseMatrix);
     }
 
     public boolean hasCachedMostSimilarLocal(Language language, int wpId) {
         boolean hasCached;
         try {
             hasCached = mostSimilarLocalMatrices != null && mostSimilarLocalMatrices.containsKey(language)
                 && mostSimilarLocalMatrices.get(language).getRow(wpId) != null;
         } catch (IOException e){
             return false;
         }
         return hasCached;
     }
 
     public SRResultList getCachedMostSimilarLocal(Language language, int wpId, int numResults, TIntSet validIds) {
         if (!hasCachedMostSimilarLocal(language, wpId)){
             return null;
         }
         SparseMatrixRow row;
         try {
             row = mostSimilarLocalMatrices.get(language).getRow(wpId);
         } catch (IOException e){
             return null;
         }
         Leaderboard leaderboard = new Leaderboard(numResults);
         for (int i=0; i<row.getNumCols() ; i++){
             int wpId2 = row.getColIndex(i);
             float value = row.getColValue(i);
             if (validIds == null || validIds.contains(wpId2)){
                 leaderboard.tallyScore(wpId2,value);
             }
         }
         SRResultList results = leaderboard.getTop();
         results.sortDescending();
         return results;
     }
 
     /**
      * Normalizers translate similarity scores to more meaningful values.
      * @param n
      */
     @Override
     public void setDefaultMostSimilarNormalizer(Normalizer n){
         defaultMostSimilarNormalizer = n;
     }
 
     @Override
     public void setDefaultSimilarityNormalizer(Normalizer defaultSimilarityNormalizer) {
         this.defaultSimilarityNormalizer = defaultSimilarityNormalizer;
     }
 
     @Override
     public void setMostSimilarNormalizer(Normalizer n, Language l){
         mostSimilarNormalizers.put((int)l.getId(),n);
     }
 
     @Override
     public void setSimilarityNormalizer(Normalizer n, Language l){
         similarityNormalizers.put((int)l.getId(),n);
     }
 
     /**
      * Throws an IllegalStateException if the model has not been mostSimilarTrained.
      */
     protected void ensureSimilarityTrained() {
         if (!defaultSimilarityNormalizer.isTrained()) {
             throw new IllegalStateException("Model default similarity has not been trained.");
         }
     }
     /**
      * Throws an IllegalStateException if the model has not been mostSimilarTrained.
      */
     protected void ensureMostSimilarTrained() {
         if (!defaultMostSimilarNormalizer.isTrained()) {
             throw new IllegalStateException("Model default mostSimilar has not been trained.");
         }
     }
 
     /**
      * Use the language-specific similarity normalizer to normalize a similarity if it exists.
      * Otherwise use the default similarity normalizer if it's available.
      * @param sr
      * @param language
      * @return
      */
     protected SRResult normalize(SRResult sr, Language language) {
         sr.score=normalize(sr.score,language);
         return sr;
     }
 
     /**
      * Use the language-specific most similar normalizer to normalize a similarity if it exists.
      * Otherwise use the default most similar normalizer if it's available.
      * @param srl
      * @param language
      * @return
      */
     protected SRResultList normalize(SRResultList srl, Language language) {
        if (mostSimilarNormalizers.containsKey((int) language.getId())
                &&mostSimilarNormalizers.get((int) language.getId()).isTrained()){
            return mostSimilarNormalizers.get((int) language.getId()).normalize(srl);
         }
         ensureMostSimilarTrained();
         return defaultMostSimilarNormalizer.normalize(srl);
     }
 
     protected double normalize (double score, Language language){
        if (similarityNormalizers.containsKey((int) language.getId())
                &&similarityNormalizers.get((int) language.getId()).isTrained()){
             return similarityNormalizers.get((int) language.getId()).normalize(score);
         }
        ensureSimilarityTrained();
        return defaultSimilarityNormalizer.normalize(score);
     }
 
     public void setNumThreads(int n) {
         this.numThreads = n;
     }
 
     @Override
     public void write(String path) throws IOException {
         ObjectOutputStream oop = new ObjectOutputStream(
                 new FileOutputStream(path + getName() + "/normalizer/defaultMostSimilarNormalizer")
         );
         oop.writeObject(defaultMostSimilarNormalizer);
         oop.flush();
         oop.close();
 
         oop = new ObjectOutputStream(
                 new FileOutputStream(path + getName() + "/normalizer/defaultSimilarityNormalizer")
         );
         oop.writeObject(defaultSimilarityNormalizer);
         oop.flush();
         oop.close();
 
         oop = new ObjectOutputStream(
                 new FileOutputStream(path + getName() + "/normalizer/mostSimilarNormalizers")
         );
         oop.writeObject(mostSimilarNormalizers);
         oop.flush();
         oop.close();
 
         oop = new ObjectOutputStream(
                 new FileOutputStream(path + getName() + "/normalizer/similarityNormalizers")
         );
         oop.writeObject(similarityNormalizers);
         oop.flush();
         oop.close();
     }
 
     @Override
     public void read(String path) throws IOException {
         try {
         ObjectInputStream oip = new ObjectInputStream(
                 new FileInputStream(path + getName() + "/normalizer/defaultMostSimilarNormalizer")
         );
         this.defaultMostSimilarNormalizer = (Normalizer)oip.readObject();
         oip.close();
 
         oip = new ObjectInputStream(
                 new FileInputStream(path + getName() + "/normalizer/defaultSimilarityNormalizer")
         );
         this.defaultSimilarityNormalizer = (Normalizer)oip.readObject();
         oip.close();
 
         oip = new ObjectInputStream(
                 new FileInputStream(path + getName() + "/normalizer/mostSimilarNormalizers")
         );
         this.mostSimilarNormalizers = (Map<Integer,Normalizer>)oip.readObject();
         oip.close();
 
         oip = new ObjectInputStream(
                 new FileInputStream(path + getName() + "/normalizer/similarityNormalizers")
         );
         this.similarityNormalizers = (Map<Integer,Normalizer>)oip.readObject();
         oip.close();
         }catch (ClassNotFoundException e){
             throw new IOException("Malformed normalizer file",e);
         }
     }
 
     @Override
     public void trainDefaultSimilarity(Dataset dataset){
         trainSimilarityNormalizer(dataset, true);
     }
 
     @Override
     public void trainSimilarity(Dataset dataset){
         trainSimilarityNormalizer(dataset, false);
     }
 
     /**
      * Trains the mostSimilarNormalizer to support the similarity() method.
      * @param dataset
      * @param isDefault
      */
     protected synchronized void trainSimilarityNormalizer(final Dataset dataset, boolean isDefault) {
         final Normalizer trainee;
         if (isDefault){
             trainee = defaultSimilarityNormalizer;
             defaultSimilarityNormalizer = new IdentityNormalizer();
 
         } else {
             trainee = similarityNormalizers.get((int)dataset.getLanguage().getId());
             similarityNormalizers.put((int)dataset.getLanguage().getId(),new IdentityNormalizer());
         }
         ParallelForEach.loop(dataset.getData(), numThreads, new Procedure<KnownSim>() {
             public void call(KnownSim ks) throws IOException, DaoException {
                 SRResult sim = similarity(ks.phrase1, ks.phrase2, ks.language, false);
                 trainee.observe(sim.getScore(), ks.similarity);
 
             }
         },1);
         trainee.observationsFinished();
         if (isDefault){
             defaultSimilarityNormalizer = trainee;
         } else {
             similarityNormalizers.put((int)dataset.getLanguage().getId(),trainee);
         }
         LOG.info("trained most similarityNormalizer for " + getName() + ": " + trainee.dump());
     }
 
     @Override
     public synchronized void trainDefaultMostSimilar(Dataset dataset, int numResults, TIntSet validIds){
         trainMostSimilarNormalizer(dataset, numResults, validIds, true);
     }
 
     @Override
     public synchronized void trainMostSimilar(Dataset dataset, int numResults, TIntSet validIds){
         trainMostSimilarNormalizer(dataset, numResults, validIds, false);
     }
 
     /**
      * Trains the mostSimilarNormalizer to support the mostSimilar() method.
      * Also estimates the similarity score for articles that don't appear in top lists.
      * Note that this (probably) is an overestimate, and depends on how well the
      * distribution of scores in your gold standard matches your actual data.
      *
      * @param dataset
      */
     protected synchronized void trainMostSimilarNormalizer(final Dataset dataset, final int numResults, final TIntSet validIds, boolean isDefault) {
         final Normalizer trainee;
         if (isDefault){
             trainee = defaultMostSimilarNormalizer;
             defaultMostSimilarNormalizer = new IdentityNormalizer();
 
         } else {
             trainee = mostSimilarNormalizers.get((int)dataset.getLanguage().getId());
             mostSimilarNormalizers.put((int)dataset.getLanguage().getId(), new IdentityNormalizer());
         }
         ParallelForEach.loop(dataset.getData(), numThreads, new Procedure<KnownSim>() {
             public void call(KnownSim ks) throws DaoException {
                 ks.maybeSwap();
                 List<LocalString> localStrings = new ArrayList<LocalString>();
                 localStrings.add(new LocalString(ks.language, ks.phrase1));
                 localStrings.add(new LocalString(ks.language, ks.phrase2));
                 List<LocalId> ids = disambiguator.disambiguate(localStrings, null);
                 LocalPage page = pageHelper.getById(ks.language,ids.get(0).getId());
                 if (page != null) {
                     SRResultList dsl = mostSimilar(page, numResults, validIds);
                     if (dsl != null) {
                         trainee.observe(dsl, dsl.getIndexForId(ids.get(1).getId()), ks.similarity);
                     }
                 }
             }
         },1);
         trainee.observationsFinished();
         if (isDefault){
             defaultMostSimilarNormalizer = trainee;
         } else {
             mostSimilarNormalizers.put((int)dataset.getLanguage().getId(),trainee);
         }
         LOG.info("trained most similar normalizer for " + getName() + ": " + trainee.dump());
     }
 
     @Override
     public abstract SRResult similarity(LocalPage page1, LocalPage page2, boolean explanations) throws DaoException;
 
     @Override
     public SRResult similarity(String phrase1, String phrase2, Language language, boolean explanations) throws DaoException {
         HashSet<LocalString> context = new HashSet<LocalString>();
         context.add(new LocalString(language,phrase2));
         LocalId similar1 = disambiguator.disambiguate(new LocalString(language, phrase1), context);
         context.clear();
         context.add(new LocalString(language,phrase1));
         LocalId similar2 = disambiguator.disambiguate(new LocalString(language,phrase2),context);
         if (similar1==null||similar2==null){
             return new SRResult();
         }
         return similarity(pageHelper.getById(language,similar1.getId()),
                 pageHelper.getById(language,similar2.getId()),
                 explanations);
     }
 
     @Override
     public abstract SRResultList mostSimilar(LocalPage page, int maxResults) throws DaoException;
 
     @Override
     public abstract SRResultList mostSimilar(LocalPage page, int maxResults, TIntSet validIds) throws DaoException;
 
     @Override
     public SRResultList mostSimilar(LocalString phrase, int maxResults) throws DaoException {
         LocalId similar = disambiguator.disambiguate(phrase,null);
         return mostSimilar(pageHelper.getById(similar.getLanguage(),similar.getId()), maxResults);
     }
 
     @Override
     public SRResultList mostSimilar(LocalString phrase, int maxResults, TIntSet validIds) throws DaoException {
         LocalId similar = disambiguator.disambiguate(phrase,null);
         if (similar==null){
             SRResultList resultList = new SRResultList(1);
             resultList.set(0, new SRResult());
             return resultList;
         }
         return mostSimilar(pageHelper.getById(similar.getLanguage(),similar.getId()), maxResults,validIds);
     }
 
     @Override
     public double[][] cosimilarity(int[] wpRowIds, int[] wpColIds, Language language) throws DaoException {
         double[][] cos = new double[wpRowIds.length][wpColIds.length];
         for (int i=0; i<wpRowIds.length; i++){
             for (int j=0; j<wpColIds.length; j++){
                 if (wpRowIds[i]==wpColIds[j]){
                     cos[i][j]=normalize(1.0,language);
                 }
                 else{
                     cos[i][j]=similarity(
                             new LocalPage(language,wpRowIds[i],null,null),
                             new LocalPage(language,wpColIds[j],null,null),
                             false).getScore();
                 }
             }
         }
         return cos;
     }
 
     @Override
     public double[][] cosimilarity(String[] rowPhrases, String[] colPhrases, Language language) throws DaoException {
         double[][] cos = new double[rowPhrases.length][colPhrases.length];
         for (int i=0; i<rowPhrases.length; i++){
             for (int j=0; j<colPhrases.length; j++){
                 if (rowPhrases[i].equals(colPhrases[j])){
                     cos[i][j]=normalize(1.0,language);
                 }
                 else{
                     cos[i][j]=similarity(rowPhrases[i],colPhrases[j],language, false).getScore();
                 }
             }
         }
         return cos;
     }
 
     @Override
     public double[][] cosimilarity(int[] ids, Language language) throws DaoException {
         double[][] cos = new double[ids.length][ids.length];
         for (int i=0; i<ids.length; i++){
             cos[i][i]=normalize(1.0,language);
         }
         for (int i=0; i<ids.length; i++){
             for (int j=i+1; j<ids.length; j++){
                 cos[i][j]=similarity(
                         new LocalPage(language, ids[i], null, null),
                         new LocalPage(language, ids[j], null, null),
                         false).getScore();
                 cos[j][i]=cos[i][j];
             }
         }
         return cos;
     }
 
     @Override
     public double[][] cosimilarity(String[] phrases, Language language) throws DaoException {
         int ids[] = new int[phrases.length];
         List<LocalString> localStringList = new ArrayList<LocalString>();
         for (String phrase : phrases){
             localStringList.add(new LocalString(language, phrase));
         }
         List<LocalId> localIds = disambiguator.disambiguate(localStringList, null);
         for (int i=0; i<phrases.length; i++){
             ids[i] = localIds.get(i).getId();
         }
         return cosimilarity(ids, language);
     }
 
 
     protected void writeCosimilarity(String path, LanguageSet languages, int maxHits, PairwiseSimilarity pairwise) throws IOException, DaoException, WikapidiaException{
         try {
             for (Language language: languages) {
                 String fullPath = path + getName() + "/matrix/" + language.getLangCode();
                 SRFeatureMatrixWriter featureMatrixWriter = new SRFeatureMatrixWriter(fullPath, this, language);
                 DaoFilter pageFilter = new DaoFilter().setLanguages(language);
                 Iterable<LocalPage> localPages = pageHelper.get(pageFilter);
                 TIntSet pageIds = new TIntHashSet();
                 for (LocalPage page : localPages) {
                     if (page != null) {
                         pageIds.add(page.getLocalId());
                     }
                 }
 
                 featureMatrixWriter.writeFeatureVectors(pageIds.toArray(), 4);
                 pairwise.initMatrices(fullPath);
                 PairwiseSimilarityWriter pairwiseSimilarityWriter = new PairwiseSimilarityWriter(fullPath,pairwise);
                 pairwiseSimilarityWriter.writeSims(pageIds.toArray(),numThreads,maxHits);
                 mostSimilarLocalMatrices.put(language,new SparseMatrix(new File(fullPath+"-cosimilarity")));
             }
         } catch (InterruptedException e){
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void readCosimilarity(String path, Language language) throws IOException {
         String fullPath = path + getName() + "/matrix/" + language.getLangCode()+"-cosimilarity";
         mostSimilarLocalMatrices.put(language,new SparseMatrix(new File(fullPath)));
     }
 
 }

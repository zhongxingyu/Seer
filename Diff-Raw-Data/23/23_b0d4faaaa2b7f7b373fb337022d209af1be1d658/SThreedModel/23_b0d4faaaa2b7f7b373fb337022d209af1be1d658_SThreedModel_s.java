 package com.tms.threed.threedModel.server;
 
 import com.tms.threed.featureModel.server.SFeatureModel;
 import com.tms.threed.featureModel.shared.FeatureModel;
 import com.tms.threed.featureModel.shared.Var;
 import com.tms.threed.featureModel.shared.picks.Picks;
 import com.tms.threed.featureModel.shared.picks.PicksRO;
 import com.tms.threed.imageModel.server.SImageModel;
 import com.tms.threed.imageModel.shared.ImSeries;
 import com.tms.threed.imageModel.shared.ImView;
 import com.tms.threed.imageModel.shared.ImageStack;
 import com.tms.threed.imageModel.shared.Jpg;
 import com.tms.threed.threedCore.shared.SeriesKey;
 import com.tms.threed.threedCore.shared.ThreedConfig;
 import com.tms.threed.threedCore.shared.ViewKey;
 import com.tms.threed.threedModel.shared.ThreedModel;
 import com.tms.threed.util.lang.shared.Path;
 import org.codehaus.jackson.node.JsonNodeFactory;
 import org.codehaus.jackson.node.ObjectNode;
 
 import java.util.List;
 import java.util.Set;
 
 public class SThreedModel {
 
     private final ThreedModel model;
 
     private final SeriesKey seriesKey;
 
     private final SThreedConfig threedConfig;
 
     private final FeatureModel featureModel;
     private final SFeatureModel sFeatureModel;
 
     private final ImSeries imageModel;
     private final SImageModel sImageModel;
 
     public SThreedModel(ThreedModel model) {
         this.model = model;
 
         this.threedConfig = new SThreedConfig(model.getThreedConfig());
         this.seriesKey = model.getSeriesKey();
         this.featureModel = model.getFeatureModel();
         this.imageModel = model.getImageModel();
 
         this.sFeatureModel = new SFeatureModel(featureModel);
         this.sImageModel = new SImageModel(imageModel);
 
     }
 
     public SThreedModel(ThreedConfig threedConfig, FeatureModel featureModel, ImSeries imageModel) {
         this(new ThreedModel(threedConfig, featureModel, imageModel));
     }
 
 
     public SeriesKey getSeriesKey() {
         return seriesKey;
     }
 
     public ThreedConfig getThreedConfig() {
         return threedConfig;
     }
 
     public Path getJpgRootFileSystem() {
         return getThreedConfig().getJpgRootFileSystem();
     }
 
 
     public Set<Var> picksToVars(Set<String> userPicks) {
         return featureModel.getVars(userPicks);
     }
 
     public Set<String> picksToCodes(Set<Var> userPicks) {
         return featureModel.varsToCodes(userPicks);
     }
 
     public Set<String> fixupPicks1(Set<String> picksRaw) {
         return featureModel.fixupPicks1(picksRaw);
     }
 
     public Picks fixupPicks(Set<String> picksRaw) {
         return featureModel.fixupPicks(picksRaw);
     }
 
     public Picks getInitialPicks() {
         return featureModel.createPicks();
     }
 
 
 //    public Set<Var> getImFeaturesMissingFromFm() {
 //        Set<Var> fmFeatures = featureModel.getFeatureCodes();
 //        Set<Var> imFeatures = imageModel.getAllFeatureCodes();
 //        return Sets.difference(imFeatures, fmFeatures);
 //    }
 //
 //    public Set<String> getFmFeaturesMissingFromIm() {
 //        Set<String> fmFeatures = featureModel.getFeatureCodes();
 //        Set<String> imFeatures = imageModel.getAllFeatureCodes();
 //        return Sets.difference(fmFeatures, imFeatures);
 //    }
 
 //    public void showFmFeaturesMissingFromIm2() {
 //        Set<String> imFeatures = imageModel.getAllFeatureCodes();
 //        List<Var> leafVars = featureModel.getLeafVars();
 //        Var varColor = featureModel.getVarOrNull(CommonVarNames.Color);
 //        Var varTrim = featureModel.getVarOrNull(CommonVarNames.Trim);
 //        for (Var leafVar : leafVars) {
 //            String code = leafVar.getCode();
 //
 //            if (imFeatures.contains(code)) continue;
 //            if (leafVar.isDescendantOf(varColor)) continue;
 //            if (leafVar.isDescendantOf(varTrim)) continue;
 //
 //
 //            String name = leafVar.getName();
 //            String title;
 //            if (!code.equals(name)) title = code + ":" + name;
 //            else title = code;
 //            System.out.println(leafVar.getParent() + ": " + title + ":" + leafVar.getExtraConstraintCount());
 //        }
 //
 //    }
 
     public ImSeries getImageModel() {
         return imageModel;
     }
 
     public SImageModel getSImageModel() {
         return sImageModel;
     }
 
     public SFeatureModel getSFeatureModel() {
         return sFeatureModel;
     }
 
     public FeatureModel getFeatureModel() {
         return featureModel;
     }
 
 
     public List<ImView> getViews() {
         return imageModel.getViews();
     }
 
     public ObjectNode toJsonNode() {
         JsonNodeFactory f = JsonNodeFactory.instance;
 
 
         ObjectNode jsSeriesKey = f.objectNode();
 
        int y = seriesKey.getYear();
        jsSeriesKey.put("year", y);
 
        String n = seriesKey.getName();
        jsSeriesKey.put("name", n);
 
         ObjectNode jsModel = f.objectNode();
 
         jsModel.put("threedConfig", threedConfig.toJsonNode());
 
         jsModel.put("seriesKey", jsSeriesKey);
 
         ObjectNode node1 = sFeatureModel.toJsonNode();
 
         jsModel.put("featureModel", node1);
 
         jsModel.put("views", sImageModel.toJsonArray());
 
         return jsModel;
     }
 
 
     public String toJsonString() {
         return toJsonNode().toString();
     }
 
 //    public Jpg getJpg(ViewKey key, int angle, Set<String> picks) {
 //        fixupPicks(picks);
 //        return imageModel.getJpg(key.getName(), angle, picks);
 //    }
 
     public Jpg getJpg(String viewName, int angle, PicksRO picks) {
         return model.getJpg(viewName, angle, picks);
     }
 
     public ImageStack getImageStack(String viewName, int angle, PicksRO picks) {
         return getJpg(viewName, angle, picks).getImageStack();
     }
 
     public List<Path> getImageUrls(String viewName, int angle, PicksRO picks) {
         ImageStack imageStack = getImageStack(viewName, angle, picks);
         Path pngRoot = threedConfig.getPngRootFileSystem();
         Path jpgRoot = threedConfig.getJpgRootFileSystem();
         return imageStack.getPaths(pngRoot, jpgRoot);
     }
 
     public List<Path> getExteriorImageUrls(int angle, PicksRO picks) {
         return getImageUrls(ViewKey.EXTERIOR, angle, picks);
     }
 
     public List<Path> getInteriorImageUrls(int angle, PicksRO picks) {
         return getImageUrls(ViewKey.INTERIOR, angle, picks);
     }
 
     public List<Path> getExteriorHeroImageUrls(PicksRO picks) {
         return getExteriorImageUrls(ViewKey.HERO_ANGLE, picks);
     }
 
     public List<Path> getInteriorDashImageUrls(PicksRO picks) {
         return getInteriorImageUrls(ViewKey.DASH_ANGLE, picks);
     }
 
     public ThreedModel getModel() {
         return model;
     }
 }

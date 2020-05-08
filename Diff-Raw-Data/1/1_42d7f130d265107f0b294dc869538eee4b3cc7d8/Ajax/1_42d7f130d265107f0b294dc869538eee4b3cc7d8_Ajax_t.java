 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controllers;
 
 import java.util.List;
 import models.Asset;
 import models.Chapter;
 import models.TextReference;
 import play.db.jpa.GenericModel;
 import play.mvc.Controller;
 
 /**
  *
  * 
  * Small ajax-functions to be called from javascript
  * 
  * 
  */
 public class Ajax extends Application {
 
     public static void getKommentar(long assetId) {
         Controller.renderHtml(new String("HEY" + assetId));
     }
 
     public static void getVariant(long assetId, int variantNum) {
         Controller.renderHtml(new String("HEY" + assetId + " : " + variantNum));
     }
 
     public static void getVariantByName(String fileName) {
         Asset variant = Asset.find("fileName = ?", fileName).first();
         if (variant == null) Controller.renderHtml("");
         Controller.renderHtml(variant.html);
     }
 
     public static void getManusByName(String fileName) {
         if (fileName == null) return;
         Asset manus = Asset.find("fileName = ?", fileName).first();
         if (manus == null) renderHtml("");
         Controller.renderHtml(manus.html);
     }
 
     public static void getReference(String fileName, String textId) {
         System.out.println("Serving popup: " + TextReference.getReference(fileName + "_" + textId));
         Controller.renderHtml(TextReference.getReference(fileName + "_" + textId));
     }
 
     public static void getRef(String textId) {
        textId = textId.replaceAll("%20", " ");
         TextReference ref = TextReference.find("textId = ?", textId).first();
         System.out.println("Showing ref: " + ref.showName);
         Controller.renderHtml(ref.showName);
     }
 
 
     public static void getIntro(long id) {
         Asset asset = Asset.find("id = ?", id).first();
         Controller.renderHtml(asset.getCorrespondingIntro());
     }
 
     public static void getComment(long id) {
         Asset asset = Asset.find("id = ?", id).first();        
         Controller.renderHtml(asset.getCorrespondingComment());
     }
 
     public static void getTxr(long id) {
         System.out.println("Looking for asset: " + id);
         Asset asset = Asset.findById(id);
         System.out.println("Asset found is: " + asset);
         Controller.renderHtml(asset.getCorrespondingTxr());
     }    
     
     public static void getVeiledning(String fileName) {
         Asset asset = Asset.find("fileName = ?", fileName + ".xml").first();
         if (asset == null) {
             Controller.renderHtml("");
         } else {
             Controller.renderHtml(asset.html);
         }
     }
     
     public static void getChapter(long assetId, int chapterNum) {
         Asset asset = Asset.findById(assetId);
         Chapter chapter = Chapter.find("byAssetAndNum", asset, chapterNum).first();
         Controller.renderHtml(chapter.html);
     }
 
 }

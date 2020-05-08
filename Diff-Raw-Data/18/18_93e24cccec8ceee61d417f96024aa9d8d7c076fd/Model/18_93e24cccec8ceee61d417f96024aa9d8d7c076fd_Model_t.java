 /*******************************************************************************
  * <eAdventure Character Configurator> is a research project of the <e-UCM>
  *          research group.
  *
  *    Developed by: Alejandro Muñoz del Rey, Sergio de Luis Nieto and David González
  *    Ledesma.
  *    Under the supervision of Baltasar Fernández-Manjón and Javier Torrente
  * 
  *    Copyright 2012-2013 <e-UCM> research group.
  *  
  *     <e-UCM> is a research group of the Department of Software Engineering
  *          and Artificial Intelligence at the Complutense University of Madrid
  *          (School of Computer Science).
  *  
  *          C Profesor Jose Garcia Santesmases sn,
  *          28040 Madrid (Madrid), Spain.
  *  
  *          For more info please visit:  <http://character.e-ucm.es>, 
  *          <http://e-adventure.e-ucm.es> or <http://www.e-ucm.es>
  *  
  *  ****************************************************************************
  *      <eAdventure Character Configurator> is free software: you can 
  *      redistribute it and/or modify it under the terms of the GNU Lesser 
  *      General Public License as published by the Free Software Foundation, 
  *      either version 3 of the License, or (at your option) any later version.
  *  
  *      <eAdventure Character Configurator> is distributed in the hope that it 
  *      will be useful, but WITHOUT ANY WARRANTY; without even the implied 
  *      warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
  *      See the GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with <eAdventure Character Configurator>. If not, 
  *      see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package control;
 
 import com.jme3.animation.AnimChannel;
 import com.jme3.animation.AnimControl;
 import com.jme3.animation.Bone;
 import com.jme3.animation.LoopMode;
 import com.jme3.animation.SkeletonControl;
 import com.jme3.app.state.ScreenshotAppState;
 import com.jme3.material.Material;
 import com.jme3.math.FastMath;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import com.jme3.renderer.ViewPort;
 import com.jme3.scene.Spatial;
 import gui.Gui;
 import control.ColoringImage;
 import control.ImagesProcessing;
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import types.Age;
 import types.Gender;
 import types.Shoes;
 import types.TShirt;
 import types.Trouser;
 import types.TypeObject;
 
 public class Model{
 
     private Spatial model;
     private HashMap<String,Spatial> subMeshes;
     private Material mat;
     private AnimChannel channel;
     private AnimControl control;
     private Age age;
     private Gender gender;
     private Vector3f vectorScaleBase;
     
     private ImagesProcessing img;
     private BufferedImage skin, trousers, tShirt, eyes, shoes;
     
     private String [] arraySkin, arraySkinIcon, arrayShoesIcon, arrayTrouserIcon, 
             arrayTShirtIcon, arrayEyes, arrayEyesIcon, namesBones;
     private TShirt [] arrayTShirt;
     private Trouser [] arrayTrouser;
     private Shoes [] arrayShoes;
     
     private int indexSkin = -1;
     private int indexTShirt = -1;
     private int indexShoes = -1;
     private int indexTrouser = -1;
     private int indexEyes = -1;
     private int numSkins;
     private int numShoes;
     private int numTrousers;
     private int numTShirts;
     private int numEyes;
     
     private int indexImage;
     private String destinationPath;
     private String modelPath;
     
     private TypeObject typeObject;
     
     public Model()
     {
         subMeshes = new HashMap<String,Spatial>();
     }
     
     public void setBodyType(int bodyType)
     {        
         Vector3f inc;
         Bone b;
         switch(bodyType)
         {
             case 0: //Normal
                     model.setLocalScale(vectorScaleBase);
                     b = control.getSkeleton().getBone("Cabeza");
                     b.setUserControl(true);
                     b.setUserTransforms(Vector3f.ZERO, Quaternion.IDENTITY,Vector3f.UNIT_XYZ);
                     break;
             case 1: //Tall
                     inc = new Vector3f(1.1f,1.25f,1.25f);
                     inc.multLocal(vectorScaleBase);
                     model.setLocalScale(inc);
                     b = control.getSkeleton().getBone("Cabeza");
                     b.setUserControl(true);
                     b.setUserTransforms(Vector3f.ZERO, Quaternion.IDENTITY,new Vector3f(1.0f/1.1f,1.0f/1.25f,1.0f/1.25f));
                     break;
             case 2: //Small
                     inc = new Vector3f(0.9f,0.75f,0.75f);
                     inc.multLocal(vectorScaleBase);
                     model.setLocalScale(inc);
                     b = control.getSkeleton().getBone("Cabeza");
                     b.setUserControl(true);
                     b.setUserTransforms(Vector3f.ZERO, Quaternion.IDENTITY,new Vector3f(1.0f/0.9f,1.0f/0.75f,1.0f/0.75f));
                     break;                
             case 3: //Heavy
                     inc = new Vector3f(1.50f,1.0f,1.0f);
                     inc.multLocal(vectorScaleBase);
                     model.setLocalScale(inc);
                     b = control.getSkeleton().getBone("Cabeza");
                     b.setUserControl(true);
                     b.setUserTransforms(Vector3f.ZERO, Quaternion.IDENTITY,new Vector3f(1.0f/1.50f,1.0f,1.0f));
                     break;
             case 4: //Thin
                     inc = new Vector3f(0.65f,1.0f,1.0f);
                     inc.multLocal(vectorScaleBase);
                     model.setLocalScale(inc);
                     b = control.getSkeleton().getBone("Cabeza");
                     b.setUserControl(true);
                     b.setUserTransforms(Vector3f.ZERO, Quaternion.IDENTITY,new Vector3f(1.0f/0.65f,1.0f,1.0f));                    
                     break;
             default: break;
                     
         }
         
     }  
     
     public void scaleHead(float inc) {
         String[] arrayNameBones = new String[1];
         arrayNameBones[0] = this.namesBones[0];
         this.scale(arrayNameBones, inc);
     }
     
     public void scaleTorax(float inc) {
         String[] arrayNameBones = new String[1];
         arrayNameBones[0] = this.namesBones[1];
         this.scale(arrayNameBones, inc);
     }
     
     public void scaleHands(float inc) {
         String[] arrayNameBones = new String[2];
         arrayNameBones[0] = this.namesBones[2];
         arrayNameBones[1] = this.namesBones[3];
         this.scale(arrayNameBones, inc);
     }
     
     public void scaleFeet(float inc) {
         String[] arrayNameBones = new String[2];
         arrayNameBones[0] = this.namesBones[4];
         arrayNameBones[1] = this.namesBones[5];
         this.scale(arrayNameBones, inc);
     }
     
     public void scaleLegs(float inc) {
         String[] arrayNameBones = new String[2];
         arrayNameBones[0] = this.namesBones[6];
         arrayNameBones[1] = this.namesBones[7];
         this.scale(arrayNameBones, inc);
     }
     
     public void scaleArms(float inc) {
         String[] arrayNameBones = new String[2];
         arrayNameBones[0] = this.namesBones[8];
         arrayNameBones[1] = this.namesBones[9];
         this.scale(arrayNameBones, inc);
     }
     
     private void scale(String[] namesBones,float inc)
     {
         for(int i = 0; i < namesBones.length ; i++)
         {
             Bone b = control.getSkeleton().getBone(namesBones[i]);
             Vector3f vector = new Vector3f(inc,inc,inc);
             b.setUserControl(true);
             b.setUserTransforms(Vector3f.ZERO,Quaternion.IDENTITY,vector);
         }
     }
     
     public void screenShot(ViewPort guiViewPort, NiftyJmeDisplay niftyDisplay, ScreenshotAppState screenShotState)
     {
         guiViewPort.removeProcessor(niftyDisplay);
         Set<String> namesAnimations = (Set<String>) control.getAnimationNames();
         //ScreenshotThread sst = new ScreenshotThread(screenShotState,channel,guiViewPort,niftyDisplay,namesAnimations);
         //sst.start();
     }
     
     public void changeSkin(int steep)
     {
         //indexSkin=indexSkin+steep;
         //if (indexSkin < 0){ indexSkin = numSkins - 1;}
         //indexSkin = indexSkin%numSkins;
         //skin = readBuffer(arraySkin[indexSkin]);
         indexSkin = steep;
         skin = readBuffer(arraySkin[steep]);
         //Creat the image             
         img = new ImagesProcessing(skin, trousers, tShirt, eyes, shoes);                
         destinationPath = "assets/Textures/FinalTexture"+indexImage+".png";                
         img.fusionaImagenes(destinationPath);
     }
     
     public void changeShoes(int steep)
     {
         /*indexShoes++;   
         shoes = readBuffer(arrayShoes[indexShoes%numShoes].getPathShoes(), arrayShoes[indexShoes%numShoes].getPathShadow(), TypeObject.shoes);*/
         indexShoes = steep;
         shoes = readBuffer(arrayShoes[steep].getPathShoes(), arrayShoes[steep].getPathShadow(), TypeObject.shoes);
         //Creat the image        
         img = new ImagesProcessing(skin, trousers, tShirt, eyes, shoes); 
         destinationPath = "assets/Textures/FinalTexture"+indexImage+".png";                
         img.fusionaImagenes(destinationPath);                
     }
     
     public void changeTrousers(int steep)
     {
         /*indexTrouser=indexTrouser+steep;
         if (indexTrouser < 0){ indexTrouser = numTrousers - 1;}
         indexTrouser = indexTrouser%numTrousers;   
         trousers = readBuffer(arrayTrouser[indexTrouser].getPathTrouser(), arrayTrouser[indexTrouser].getPathShadow(), TypeObject.trouser);*/
         indexTrouser = steep;
         trousers = readBuffer(arrayTrouser[steep].getPathTrouser(), arrayTrouser[steep].getPathShadow(), TypeObject.trouser);
         //Creat the image        
         img = new ImagesProcessing(skin, trousers, tShirt, eyes, shoes);     
         destinationPath = "assets/Textures/FinalTexture"+indexImage+".png";        
         img.fusionaImagenes(destinationPath);        
     }
     
     public void changeTShirt(int steep)
     {
         /*indexTShirt=indexTShirt+steep;
         if (indexTShirt < 0){ indexTShirt = numTShirts - 1;}
         indexTShirt = indexTShirt%numTShirts;
         tShirt = readBuffer(arrayTShirt[indexTShirt].getPathTShirt(), arrayTShirt[indexTShirt].getPathShadow(), TypeObject.t_shirt);*/
         indexTShirt = steep;
         tShirt = readBuffer(arrayTShirt[steep].getPathTShirt(), arrayTShirt[steep].getPathShadow(), TypeObject.t_shirt);
         //Creat the image        
         img = new ImagesProcessing(skin, trousers, tShirt, eyes, shoes);     
         destinationPath = "assets/Textures/FinalTexture"+indexImage+".png";        
         img.fusionaImagenes(destinationPath);        
     }
     
     public void changeEyes(int steep)
     {
         /*indexEyes=indexEyes+steep;
         if (indexEyes < 0){ indexEyes = numEyes - 1;}
         indexEyes = indexEyes%numEyes;
         eyes = readBuffer(arrayEyes[indexEyes]);*/
         //Creat the image
         indexEyes = steep;
         eyes = readBuffer(arrayEyes[steep]);
         img = new ImagesProcessing(skin, trousers, tShirt, eyes, shoes); 
         destinationPath = "assets/Textures/FinalTexture"+indexImage+".png";        
         img.fusionaImagenes(destinationPath);              
     }
     
     public void changeColor(Color color) throws IOException
     {
         ColoringImage coloringImage;
         switch(typeObject) {
             case t_shirt:
                 coloringImage = new  ColoringImage(arrayTShirt[indexTShirt].getPathTShirt(), arrayTShirt[indexTShirt].getPathShadow(), color, TypeObject.t_shirt); 
                 tShirt = coloringImage.coloringImage();      
                 break;
             case trouser:
                 coloringImage = new  ColoringImage(arrayTrouser[indexTrouser].getPathTrouser(), arrayTrouser[indexTrouser].getPathShadow(), color, TypeObject.trouser);
                 trousers = coloringImage.coloringImage(); 
                 break;      
             case shoes:
                 coloringImage = new  ColoringImage(arrayShoes[indexShoes].getPathShoes(), arrayShoes[indexShoes].getPathShadow(), color, TypeObject.shoes);
                 shoes = coloringImage.coloringImage();
                 break; 
         }    
         img = new ImagesProcessing(skin, trousers, tShirt, eyes, shoes); 
         destinationPath = "assets/Textures/FinalTexture"+indexImage+".png"; 
         img.fusionaImagenes(destinationPath); 
     }
 
     public void readBuffers()
     {
         indexSkin++;
         String skinsPath = arraySkin[indexSkin];
         indexShoes++;
         String shoesColorPath = arrayShoes[indexShoes].getPathShoes();
         String shoesShadowPath = arrayShoes[indexShoes].getPathShadow();
         indexTrouser++;
         String trousersColorPath = arrayTrouser[indexTrouser].getPathTrouser();
         String trousersShadowPath = arrayTrouser[indexTrouser].getPathShadow();
         indexTShirt++;
         String tShirtsColorPath = arrayTShirt[indexTShirt].getPathTShirt();
         String tShirtsShadowPath = arrayTShirt[indexTShirt].getPathShadow();
         indexEyes++;
         String eyesPath = arrayEyes[indexEyes]; 
         
         try {
             skin = ImageIO.read(new File(skinsPath));
             ColoringImage coloredImage = new ColoringImage(trousersColorPath, trousersShadowPath, new Color(0, 255, 0), TypeObject.trouser);
             trousers = coloredImage.coloringImage();
             coloredImage = new ColoringImage(tShirtsColorPath, tShirtsShadowPath, new Color(0, 255, 0), TypeObject.t_shirt);
             tShirt = coloredImage.coloringImage();
             eyes = ImageIO.read(new File(eyesPath));
             coloredImage = new ColoringImage(shoesColorPath, shoesShadowPath, new Color(0, 255, 0), TypeObject.shoes);
             shoes = coloredImage.coloringImage();
         } catch (IOException ex) {
             Logger.getLogger(Gui.class.getName()).log(Level.SEVERE, null, ex);
         }     
         img = new ImagesProcessing(skin, trousers, tShirt, eyes, shoes);
         destinationPath = "assets/Textures/OriginalTexture.png";
         img.fusionaImagenes(destinationPath);       
     }
     
     private BufferedImage readBuffer(String path)
     {
         try {
             BufferedImage aux = ImageIO.read(new File(path));
             return aux;
         } catch (IOException ex) {
             Logger.getLogger(Gui.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
     }
     
     private BufferedImage readBuffer(String pathColor, String pathShadow, TypeObject typeObject)
     {
         try {
             ColoringImage coloredImage = new ColoringImage(pathColor, pathShadow, new Color(0, 255, 0), typeObject);
             BufferedImage aux = coloredImage.coloringImage();
             return aux;
         } catch (IOException ex) {
             Logger.getLogger(Gui.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
     }
 
     public Spatial getModel() 
     {
         return model;
     }
 
     public void setModel(Spatial model,Material mat) 
     {
         this.model = model;
         this.mat = mat;
         this.model.setMaterial(mat);
         this.control = model.getControl(AnimControl.class);
         this.channel = this.control.createChannel();
         Iterator<String> it = this.control.getAnimationNames().iterator();
         this.channel.setAnim(it.next());
         this.channel.setLoopMode(LoopMode.Loop);       
     }
     
     public void setPositionModel()
     {
         this.model.rotate(FastMath.DEG_TO_RAD * 90,0.0f,0.0f);
         this.model.move(0.0f,-3.0f,0.0f);
         this.vectorScaleBase = new Vector3f(1.0f,1.0f,1.0f);
         if (this.age == Age.Young){
             //Scale the model
             this.model.scale(0.65f, 0.65f, 0.65f);
             this.vectorScaleBase.set(0.65f,0.65f,0.65f);
         }
     }
     
     public void addSubMeshes(String bone,Spatial subMesh)
     {
            subMeshes.put(bone, subMesh);
            SkeletonControl control = this.model.getControl(SkeletonControl.class);
            control.getAttachmentsNode(bone).attachChild(subMesh);           
            subMesh.rotate(FastMath.DEG_TO_RAD * 90,0.0f,0.0f);
           subMesh.move(2.10f,-4.80f,0.63f);
            //Para todos los pelos
            //hairMesh.move(0.0f,0.3f,0.0f);
            //Para el pelo goku
            subMesh.move(0.0f,0.5f,0.0f);
     }
     
     public void dettachAllChild()
     {
         SkeletonControl control = this.model.getControl(SkeletonControl.class);
         Set<String> bones = subMeshes.keySet();
         Iterator<String> it = bones.iterator();
         while(it.hasNext())
         {
             String bone = it.next();
             Spatial subMesh = subMeshes.get(bone); 
             control.getAttachmentsNode(bone).detachChild(subMesh);
         }
     }
     
     public void attachAllChild()
     {
        SkeletonControl control = this.model.getControl(SkeletonControl.class);
         Set<String> bones = subMeshes.keySet();
         Iterator<String> it = bones.iterator();
         while(it.hasNext())
         {
             String bone = it.next();
             Spatial subMesh = subMeshes.get(bone); 
             control.getAttachmentsNode(bone).attachChild(subMesh);
         } 
     }
     
     public Material getMaterial() 
     {
         return mat;
     }
 
     public void setMaterial(Material mat) 
     {
         this.mat = mat;
         this.model.setMaterial(this.mat);
     }
     
     public Age getAge() 
     {
         return age;
     }
 
     public void setAge(Age age) 
     {
         this.age = age; 
     }
     
     public Gender getGender()
     {
         return gender;
     }
     
     public void setGender(Gender gender)
     {
         this.gender = gender;
     }
 
     public void setIndexImage(int indexImage) 
     {
         this.indexImage = indexImage;
     }   
     
     public TypeObject getTypeObject() 
     {
         return typeObject;
     }
 
     public void setTypeObject(TypeObject typeObject) 
     {
         this.typeObject = typeObject;
     }
 
     public void setArraySkin(String[] arraySkin) 
     {
         this.arraySkin = arraySkin;
     }
 
     public void setArrayShoes(Shoes[] arrayShoes) 
     {
         this.arrayShoes = arrayShoes;
     }
 
     public void setArrayTrouser(Trouser[] arrayTrouser) 
     {
         this.arrayTrouser = arrayTrouser;
     }
 
     public void setArrayTShirt(TShirt[] arrayTShirt) 
     {
         this.arrayTShirt = arrayTShirt;
     }
 
     public void setArrayEyes(String[] arrayEyes) 
     {
         this.arrayEyes = arrayEyes;
     }
 
     public void setNumSkins(int numSkins) 
     {
         this.numSkins = numSkins;
     }
 
     public void setNumShoes(int numShoes) 
     {
         this.numShoes = numShoes;
     }
 
     public void setNumTrousers(int numTrousers) 
     {
         this.numTrousers = numTrousers;
     }
 
     public void setNumTShirts(int numTShirts) 
     {
         this.numTShirts = numTShirts;
     }
 
     public void setNumEyes(int numEyes) 
     {
         this.numEyes = numEyes;
     }
     
     public String getModelPath() 
     {
         return modelPath;
     }
     
     public void setModelPath(String modelPath) 
     {
         this.modelPath = modelPath;
     }
 
     public void setArraySkinIcon(String[] arraySkinIcon) {
         this.arraySkinIcon = arraySkinIcon;
     }
 
     public void setArrayEyesIcon(String[] arrayEyesIcon) {
         this.arrayEyesIcon = arrayEyesIcon;
     }
 
     public void setArrayTShirtIcon(String[] arrayTShirtIcon) {
         this.arrayTShirtIcon = arrayTShirtIcon;
     }
 
     public void setArrayTrouserIcon(String[] arrayTrouserIcon) {
         this.arrayTrouserIcon = arrayTrouserIcon;
     }
 
     public void setArrayShoesIcon(String[] arrayShoesIcon) {
         this.arrayShoesIcon = arrayShoesIcon;
     }
     public String[] getArraySkinIcon() {
         return arraySkinIcon;
     }
 
     public String[] getArrayEyesIcon() {
         return arrayEyesIcon;
     }
 
     public String[] getArrayTShirtIcon() {
         return arrayTShirtIcon;
     }
 
     public String[] getArrayTrouserIcon() {
         return arrayTrouserIcon;
     }
 
     public String[] getArrayShoesIcon() {
         return arrayShoesIcon;
     }
     
     public String[] getNamesBones() {
         return namesBones;
     }
 
     public void setNamesBones(String[] namesBones) {
         this.namesBones = namesBones;
     }
 }

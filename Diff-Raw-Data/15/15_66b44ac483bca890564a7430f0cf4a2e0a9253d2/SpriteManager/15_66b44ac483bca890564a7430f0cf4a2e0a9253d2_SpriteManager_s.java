 package oms.GameEngine;
 
 import info.pharos.ly.KLog;
 
 
 public class SpriteManager extends Object  {
 
 // annotations
 // value = {
 // Loms/GameEngine/SpriteManager$SpriteACTFILE;,
 // Loms/GameEngine/SpriteManager$SpriteRESACTINFO;
 // }
 // instance fields
 private oms.GameEngine.ACTStruct ACTStructInfo;
 private oms.GameEngine.SpriteDEF[] Sprite;
 private oms.GameEngine.SpriteResDEF[] SpriteRes;
 private oms.GameEngine.SpriteManager.SpriteRESACTINFO[] SpriteResACTInfo;
 private boolean bLoadingSpriteRes;
 private android.graphics.Matrix cMatrix;
 private oms.GameEngine.SpriteManager.SpriteACTFILE cSpriteACTFile;
 private int mACTLibBeg;
 private android.content.Context mContext;
 private boolean mIsLogOut;
 private long nCurBMPRamSize;
 private int nCurFlushSpriteNum;
 private int nCurSegLoadedSpriteNum;
 private int nCurSpriteFileNum;
 private int nMaxSpriteNum;
 private int nMaxSpriteResNum;
 private int nScreenOrientation;
 private int nShowSpriteNum;
 private android.graphics.Rect nViewRc;
 public oms.GameEngine.SpriteResSeg[] pSpriteResSegInfo;
 // direct methods
 
 public SpriteManager(android.content.Context context,int SpriteResNum,int SpriteNum){
 	super();
 	this.mIsLogOut = true;
 	this.nCurBMPRamSize = 0 /* 0 */;
 	this.nScreenOrientation = 0 /* 0 */;
 	this.mACTLibBeg = 0 /* 0 */;
 	this.mContext = context;
 	this.Init();
 	this.InitSpriteRes(SpriteResNum);
 	this.InitSprite(SpriteNum);
 	this.InitSpriteACTFile();
 	this.nViewRc = oms.GameEngine.GameCanvas.GetViewRect();
 	;
 	this.cMatrix = new android.graphics.Matrix();
 }
 
 private int GetBitmapSize(android.graphics.Bitmap bitmap){
 	int bit = 1;
 	if (bitmap.getConfig() == android.graphics.Bitmap.Config.ALPHA_8) {
 	bit = 1;
 	// :cond_0
 	} else {
 	if (bitmap.getConfig() == android.graphics.Bitmap.Config.RGB_565) {
 	bit = 2;
 	// goto :goto_0
 	} else {
 	if (bitmap.getConfig() == android.graphics.Bitmap.Config.ARGB_4444) {
 	bit = 2;
 	// goto :goto_0
 	} else {
 	if (bitmap.getConfig() == android.graphics.Bitmap.Config.ARGB_8888) {
 	bit = 4;
 		}
 		}
 		}
 		}
 	// goto :goto_0
 	// :goto_0
 	;
 	return (((bitmap.getWidth() * bitmap.getHeight()) * bit) + 0x80);
 	//return (((bitmap.getWidth() * bitmap.getHeight()) * bit) + 0x80);
 }
 
 private void GetFileHead(int ACTLibId){
 	int nBuffBeg = 0;
 	nBuffBeg = (ACTLibId * 0x4);
 	byte[] buff = this.cSpriteACTFile.pACTBuff;
 	this.ACTStructInfo.FileNum = 0 /* 0 */;
 	int i = 0;
 	while (i < 2) {
 	int temp = (buff[(nBuffBeg + i)] & 0xff);
 	temp = (temp << (i * 0x8));
 	this.ACTStructInfo.FileNum = (this.ACTStructInfo.FileNum + temp);
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_0
 	}
 	this.ACTStructInfo.FileIndexAddr = 0 /* 0 */;
 	i = 0 /* 0 */;
 	while (i < 2) {
 	int temp = (buff[((nBuffBeg + 0x2) + i)] & 0xff);
 	
 	temp = (temp << (i * 0x8));
 	this.ACTStructInfo.FileIndexAddr = (this.ACTStructInfo.FileIndexAddr + temp);
 	i = (i + 0x1);
 	// goto :goto_1
 	// :cond_1
 	}
 	return;
 	//[OTHER] end local v3           #temp:I
 }
 
 private void GetFileHead(oms.GameEngine.RESApp rs,int ACTId){
 	int FileHeadSize = 4;
 	;
 	byte[] buff = new byte[FileHeadSize];
 	rs.ResRead(buff,(ACTId * FileHeadSize),FileHeadSize);
 	this.ACTStructInfo.FileNum = 0 /* 0 */;
 	int i = 0;
 	while (i < 2) {
 	int temp = (buff[i] & 0xff);
 	temp = (temp << (i * 0x8));
 	this.ACTStructInfo.FileNum = (this.ACTStructInfo.FileNum + temp);
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_0
 	}
 	this.ACTStructInfo.FileIndexAddr = 0 /* 0 */;
 	i = 0 /* 0 */;
 	while (i < 2) {
 	int temp = (buff[(i + 0x2)] & 0xff);
 	
 	temp = (temp << (i * 0x8));
 	this.ACTStructInfo.FileIndexAddr = (this.ACTStructInfo.FileIndexAddr + temp);
 	i = (i + 0x1);
 	// goto :goto_1
 	// :cond_1
 	}
 	return;
 	//[OTHER] end local v3           #temp:I
 }
 
 private void GetSpriteHead(int ACTLibId){
 	int nBuffBeg = ((ACTLibId * 0x8) + this.ACTStructInfo.FileIndexAddr);
 	byte[] buff = this.cSpriteACTFile.pACTBuff;
 	this.ACTStructInfo.SpriteNum = 0 /* 0 */;
 	int i = 0;
 	while (i < 4) {
 	int temp = (buff[(nBuffBeg + i)] & 0xff);
 	temp = (temp << (i * 0x8));
 	this.ACTStructInfo.SpriteNum = (this.ACTStructInfo.SpriteNum + temp);
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_0
 	}
 	this.ACTStructInfo.SpriteIndexAddr = 0 /* 0 */;
 	i = 0 /* 0 */;
 	while (i < 4) {
 	int temp = (buff[((nBuffBeg + i) + 0x4)] & 0xff);
 	
 	temp = (temp << (i * 0x8));
 	this.ACTStructInfo.SpriteIndexAddr = (this.ACTStructInfo.SpriteIndexAddr + temp);
 	i = (i + 0x1);
 	// goto :goto_1
 	// :cond_1
 	}
 	return;
 	//[OTHER] end local v3           #temp:I
 }
 
 private int GetSpriteResID(int ACTFileId){
 	int SpriteInfoOffset = 20;
 	byte[] buff = this.cSpriteACTFile.pACTBuff;
 	SpriteInfoOffset = ((ACTFileId * SpriteInfoOffset) + this.ACTStructInfo.SpriteIndexAddr);
 	int ResID = 0;
 	int i = 0;
 	while (i < 4) {
 	int temp = (buff[(SpriteInfoOffset + i)] & 0xff);
 	temp = (temp << (i * 0x8));
 	ResID = (ResID + temp);
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_0
 	}
 	return ResID;
 }
 
 private void Init(){
 	this.SpriteRes = null /* 0 */;
 	this.nMaxSpriteResNum = 0 /* 0 */;
 	this.SpriteResACTInfo = null /* 0 */;
 	this.nCurSpriteFileNum = 0 /* 0 */;
 	this.bLoadingSpriteRes = false /* 0 */;
 	this.pSpriteResSegInfo = null /* 0 */;
 	this.nCurSegLoadedSpriteNum = 0 /* 0 */;
 	this.ACTStructInfo = null /* 0 */;
 	this.Sprite = null /* 0 */;
 	this.nMaxSpriteNum = 0 /* 0 */;
 	this.nShowSpriteNum = 0 /* 0 */;
 	this.nCurFlushSpriteNum = 0 /* 0 */;
 	this.nCurBMPRamSize = 0 /* 0 */;
 	this.mACTLibBeg = 0 /* 0 */;
 	return;
 }
 
 private void ReadSpriteResInfo(int ACTFileId,int ACTLibId){
 	android.graphics.Bitmap bmp = null;
 	int SpriteInfoOffset = 20;
 	byte[] buff = this.cSpriteACTFile.pACTBuff;
 	SpriteInfoOffset = ((ACTFileId * SpriteInfoOffset) + this.ACTStructInfo.SpriteIndexAddr);
 	this.ACTStructInfo.ResID = 0 /* 0 */;
 	int i = 0;
 	while (i < 4) {
 	int temp = (buff[(SpriteInfoOffset + i)] & 0xff);
 	temp = (temp << (i * 0x8));
 	this.ACTStructInfo.ResID = (this.ACTStructInfo.ResID + temp);
 	i = (i + 0x1);
 	// goto/16 :goto_0
 	// :cond_a
 	}
 	SpriteInfoOffset = (SpriteInfoOffset + 0x4);
 	this.ACTStructInfo.CenterX = 0 /* 0 */;
 	i = 0 /* 0 */;
 	while (i < 2) {
 	int temp = (buff[(SpriteInfoOffset + i)] & 0xff);
 	
 	temp = (temp << (i * 0x8));
 	this.ACTStructInfo.CenterX = (this.ACTStructInfo.CenterX + temp);
 	i = (i + 0x1);
 	// goto/16 :goto_1
 	// :cond_b
 	}
 	if ((this.ACTStructInfo.CenterX & 32768) == 32768) {
 	//(0x1 - this.ACTStructInfo.CenterX) = -(0x1 - this.ACTStructInfo.CenterX);
 	this.ACTStructInfo.CenterX = -(0x1 - this.ACTStructInfo.CenterX);
 	// :cond_0
 	}
 	SpriteInfoOffset = (SpriteInfoOffset + 0x2);
 	this.ACTStructInfo.CenterY = 0 /* 0 */;
 	i = 0 /* 0 */;
 	while (i < 2) {
 	int temp = (buff[(SpriteInfoOffset + i)] & 0xff);
 	
 	temp = (temp << (i * 0x8));
 	this.ACTStructInfo.CenterY = (this.ACTStructInfo.CenterY + temp);
 	i = (i + 0x1);
 	// goto/16 :goto_2
 	// :cond_c
 	}
 	if ((this.ACTStructInfo.CenterY & 32768) == 32768) {
 	//(0x1 - this.ACTStructInfo.CenterY) = -(0x1 - this.ACTStructInfo.CenterY);
 	this.ACTStructInfo.CenterY = -(0x1 - this.ACTStructInfo.CenterY);
 	// :cond_1
 	}
 	SpriteInfoOffset = (SpriteInfoOffset + 0x2);
 	this.ACTStructInfo.XHitL = 0 /* 0 */;
 	i = 0 /* 0 */;
 	while (i < 2) {
 	int temp = (buff[(SpriteInfoOffset + i)] & 0xff);
 	
 	temp = (temp << (i * 0x8));
 	this.ACTStructInfo.XHitL = (this.ACTStructInfo.XHitL + temp);
 	i = (i + 0x1);
 	// goto/16 :goto_3
 	// :cond_d
 	}
 	if ((this.ACTStructInfo.XHitL & 32768) == 32768) {
 	//(0x1 - this.ACTStructInfo.XHitL) = -(0x1 - this.ACTStructInfo.XHitL);
 	this.ACTStructInfo.XHitL = -(0x1 - this.ACTStructInfo.XHitL);
 	// :cond_2
 	}
 	SpriteInfoOffset = (SpriteInfoOffset + 0x2);
 	this.ACTStructInfo.XHitR = 0 /* 0 */;
 	i = 0 /* 0 */;
 	while (i < 2) {
 	int temp = (buff[(SpriteInfoOffset + i)] & 0xff);
 	
 	temp = (temp << (i * 0x8));
 	this.ACTStructInfo.XHitR = (this.ACTStructInfo.XHitR + temp);
 	i = (i + 0x1);
 	// goto/16 :goto_4
 	// :cond_e
 	}
 	if ((this.ACTStructInfo.XHitR & 32768) == 32768) {
 	//(0x1 - this.ACTStructInfo.XHitR) = -(0x1 - this.ACTStructInfo.XHitR);
 	this.ACTStructInfo.XHitR = -(0x1 - this.ACTStructInfo.XHitR);
 	// :cond_3
 	}
 	SpriteInfoOffset = (SpriteInfoOffset + 0x2);
 	this.ACTStructInfo.YHitU = 0 /* 0 */;
 	i = 0 /* 0 */;
 	while (i < 2) {
 	int temp = (buff[(SpriteInfoOffset + i)] & 0xff);
 	
 	temp = (temp << (i * 0x8));
 	this.ACTStructInfo.YHitU = (this.ACTStructInfo.YHitU + temp);
 	i = (i + 0x1);
 	// goto/16 :goto_5
 	// :cond_f
 	}
 	if ((this.ACTStructInfo.YHitU & 32768) == 32768) {
 	//(0x1 - this.ACTStructInfo.YHitU) = -(0x1 - this.ACTStructInfo.YHitU);
 	this.ACTStructInfo.YHitU = -(0x1 - this.ACTStructInfo.YHitU);
 	// :cond_4
 	}
 	SpriteInfoOffset = (SpriteInfoOffset + 0x2);
 	this.ACTStructInfo.YHitD = 0 /* 0 */;
 	i = 0 /* 0 */;
 	while (i < 2) {
 	int temp = (buff[(SpriteInfoOffset + i)] & 0xff);
 	
 	temp = (temp << (i * 0x8));
 	this.ACTStructInfo.YHitD = (this.ACTStructInfo.YHitD + temp);
 	i = (i + 0x1);
 	// goto/16 :goto_6
 	// :cond_10
 	}
 	if ((this.ACTStructInfo.YHitD & 32768) == 32768) {
 	//(0x1 - this.ACTStructInfo.YHitD) = -(0x1 - this.ACTStructInfo.YHitD);
 	this.ACTStructInfo.YHitD = -(0x1 - this.ACTStructInfo.YHitD);
 	// :cond_5
 	}
 	SpriteInfoOffset = (SpriteInfoOffset + 0x2);
 	this.ACTStructInfo.ZHitF = 0 /* 0 */;
 	i = 0 /* 0 */;
 	while (i < 2) {
 	int temp = (buff[(SpriteInfoOffset + i)] & 0xff);
 	
 	temp = (temp << (i * 0x8));
 	this.ACTStructInfo.ZHitF = (this.ACTStructInfo.ZHitF + temp);
 	i = (i + 0x1);
 	// goto/16 :goto_7
 	// :cond_11
 	}
 	if ((this.ACTStructInfo.ZHitF & 32768) == 32768) {
 	//(0x1 - this.ACTStructInfo.ZHitF) = -(0x1 - this.ACTStructInfo.ZHitF);
 	this.ACTStructInfo.ZHitF = -(0x1 - this.ACTStructInfo.ZHitF);
 	// :cond_6
 	}
 	SpriteInfoOffset = (SpriteInfoOffset + 0x2);
 	this.ACTStructInfo.ZHitB = 0 /* 0 */;
 	i = 0 /* 0 */;
 	while (i < 2) {
 	int temp = (buff[(SpriteInfoOffset + i)] & 0xff);
 	
 	temp = (temp << (i * 0x8));
 	this.ACTStructInfo.ZHitB = (this.ACTStructInfo.ZHitB + temp);
 	i = (i + 0x1);
 	// goto/16 :goto_8
 	// :cond_12
 	}
 	if ((this.ACTStructInfo.ZHitB & 32768) == 32768) {
 	//(0x1 - this.ACTStructInfo.ZHitB) = -(0x1 - this.ACTStructInfo.ZHitB);
 	this.ACTStructInfo.ZHitB = -(0x1 - this.ACTStructInfo.ZHitB);
 	// :cond_7
 	}
 	SpriteInfoOffset = (SpriteInfoOffset + 0x2);
 	bmp = android.graphics.BitmapFactory.decodeResource(this.mContext.getResources(),this.ACTStructInfo.ResID);
 	if (bmp == null) {
 	bmp = oms.GameEngine.PackageManager.loadPic(this.mContext,this.ACTStructInfo.ResID);
 	// :cond_8
 	}
 	if (bmp != null) {
 	int nResId = (this.ACTStructInfo.ResID & 65535);
 	if (nResId >= this.nMaxSpriteResNum) {
 	//[OTHER] end local v4           #nResId:I
 	} else {
 	this.nCurBMPRamSize = (this.nCurBMPRamSize + (long)this.GetBitmapSize(bmp));
 	if (this.mIsLogOut) {
 	android.util.Log.v("Sprite",new StringBuilder("ResID: ").append(this.ACTStructInfo.ResID).toString());
 	android.util.Log.v("Sprite",new StringBuilder("Width: ").append(bmp.getWidth()).toString());
 	android.util.Log.v("Sprite",new StringBuilder("Height: ").append(bmp.getHeight()).toString());
 	android.util.Log.v("Sprite",new StringBuilder("OPtions: ").append(bmp.getConfig()).toString());
 	android.util.Log.v("Sprite",new StringBuilder("Picture use RAM: ").append((this.nCurBMPRamSize / 1024)).append(" KBytes").toString());
 	// :cond_14
 	}
 	this.SpriteRes[nResId].Sprite = bmp;
 	this.SpriteRes[nResId].SpriteResID = this.ACTStructInfo.ResID;
 	this.SpriteRes[nResId].SpriteCenterX = this.ACTStructInfo.CenterX;
 	this.SpriteRes[nResId].SpriteCenterY = this.ACTStructInfo.CenterY;
 	this.SpriteRes[nResId].SpriteXHitL = this.ACTStructInfo.XHitL;
 	this.SpriteRes[nResId].SpriteXHitR = this.ACTStructInfo.XHitR;
 	this.SpriteRes[nResId].SpriteYHitU = this.ACTStructInfo.YHitU;
 	this.SpriteRes[nResId].SpriteYHitD = this.ACTStructInfo.YHitD;
 	this.SpriteRes[nResId].SpriteZHitF = this.ACTStructInfo.ZHitF;
 	this.SpriteRes[nResId].SpriteZHitB = this.ACTStructInfo.ZHitB;
 	this.SpriteResACTInfo[nResId].RESACTIdx = (short)ACTLibId;
 	this.pSpriteResSegInfo[ACTLibId].SegSize = (this.pSpriteResSegInfo[ACTLibId].SegSize + (((bmp.getWidth() * bmp.getHeight()) * 0x4) + 0x80));
 	bmp = null /* 0 */;
 		}
 	// goto/16 :goto_9
 	// :cond_9
 	}
 	// :goto_9
 	return;
 	//[OTHER] end local v5           #temp:I
 	//[OTHER] end local v5           #temp:I
 	//[OTHER] end local v5           #temp:I
 	//[OTHER] end local v5           #temp:I
 	//[OTHER] end local v5           #temp:I
 	//[OTHER] end local v5           #temp:I
 	//[OTHER] end local v5           #temp:I
 	//[OTHER] end local v5           #temp:I
 	//[OTHER] end local v5           #temp:I
 	
 }
 // virtual methods
 
 public boolean CHKACTTouch(int SACTName,int SXVal,int SYVal,int DACTName,int DXVal,int DYVal){
 	int SXHitL = this.GetSpriteXHitL(SACTName);
 	int SXHitR = this.GetSpriteXHitR(SACTName);
 	int DXHitL = this.GetSpriteXHitL(DACTName);
 	int DXHitR = this.GetSpriteXHitR(DACTName);
 	if (oms.GameEngine.GameMath.CHKTouch(SXHitL,SXVal,SXHitR,DXHitL,DXVal,DXHitR)) {
 	int SYHitU = this.GetSpriteYHitU(SACTName);
 	int SYHitD = this.GetSpriteYHitD(SACTName);
 	int DYHitU = this.GetSpriteYHitU(DACTName);
 	int DYHitD = this.GetSpriteYHitD(DACTName);
 	if (oms.GameEngine.GameMath.CHKTouch(SYHitU,SYVal,SYHitD,DYHitU,DYVal,DYHitD)) {
 	//[OTHER] end local v8           #SYHitU:I
 	//[OTHER] end local v10           #SYHitD:I
 	//[OTHER] end local v11           #DYHitU:I
 	//[OTHER] end local v13           #DYHitD:I
 	// :cond_0
 		return true;
 	}
 	;
 	// goto :goto_0
 		}
 	return false /* 0 */;
 }
 
 public boolean CHKACTTouch(int SXVal,int SYVal,int SXHitL,int SXHitR,int SYHitU,int SYHitD,int DACTName,int DXVal,int DYVal){
 	int DXHitL = this.GetSpriteXHitL(DACTName);
 	int DXHitR = this.GetSpriteXHitR(DACTName);
 	if (oms.GameEngine.GameMath.CHKTouch(SXHitL,SXVal,SXHitR,DXHitL,DXVal,DXHitR)) {
 	int DYHitU = this.GetSpriteYHitU(DACTName);
 	int DYHitD = this.GetSpriteYHitD(DACTName);
 	if (oms.GameEngine.GameMath.CHKTouch(SYHitU,SYVal,SYHitD,DYHitU,DYVal,DYHitD)) {
 	//[OTHER] end local v11           #DYHitU:I
 	//[OTHER] end local v13           #DYHitD:I
 	// :cond_0
 		return true;
 	}
 	}
 	;
 	return false /* 0 */;
 	// goto :goto_0
 }
 
 public void ClearACT(){
 	int i = 0;
 	while (i < this.nShowSpriteNum) {
 	this.Sprite[i].SpriteResID = -1;
 	this.Sprite[i].Alpha = 255;
 	this.Sprite[i].Rotate = 0 /* 0 */;
 	this.Sprite[i].ScaleX = 1.0F;
 	this.Sprite[i].ScaleY = 1.0F;
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_0
 	}
 	this.nShowSpriteNum = 0 /* 0 */;
 	return;
 }
 
 public void CloseSpriteACTFile(){
 	this.cSpriteACTFile.release();
 	return;
 }
 
 public void FreeACT(int ACTLibIdx){
 	ACTLibIdx = (ACTLibIdx + this.mACTLibBeg);
 	int i = 0;
 	while (i < this.nMaxSpriteResNum) {
 	if (this.SpriteResACTInfo[i].RESACTIdx == ACTLibIdx) {
 	if (this.SpriteRes[i].Sprite != null) {
 	int Size = this.GetBitmapSize(this.SpriteRes[i].Sprite);
 	this.nCurBMPRamSize = (this.nCurBMPRamSize - (long)Size);
 	this.pSpriteResSegInfo[ACTLibIdx].SegSize = (this.pSpriteResSegInfo[ACTLibIdx].SegSize - Size);
 	this.pSpriteResSegInfo[ACTLibIdx].SegIdx = 1024;
 	this.SpriteRes[i].Sprite.recycle();
 	//[OTHER] end local v0           #Size:I
 	// :cond_2
 	}
 	this.SpriteRes[i].Sprite = null /* 0 */;
 	this.SpriteRes[i].SpriteResID = 1024;
 	this.SpriteResACTInfo[i].RESACTIdx = 1024;
 	// :cond_3
 	}
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_1
 	}
 	if (this.mIsLogOut) {
 	android.util.Log.v("GameEngine",new StringBuilder("Picture use RAM: ").append((this.nCurBMPRamSize / 1024)).append(" KBytes").toString());
 	// :cond_0
 	}
 	System.gc();
 	return;
 }
 
 public void FreeACT(int ACTLibId,int ACTFrameID){
 	if (ACTFrameID < 0x7f02) {
 	// :cond_0
 	} else {
 	ACTLibId = (ACTLibId + this.mACTLibBeg);
 	ACTFrameID = (ACTFrameID & 65535);
 	if (ACTFrameID < this.nMaxSpriteResNum && this.SpriteResACTInfo[ACTFrameID].RESACTIdx != -1) {
 	if (this.SpriteRes[ACTFrameID].Sprite != null) {
 	int Size = this.GetBitmapSize(this.SpriteRes[ACTFrameID].Sprite);
 	this.nCurBMPRamSize = (this.nCurBMPRamSize - (long)Size);
 	this.pSpriteResSegInfo[ACTLibId].SegSize = (this.pSpriteResSegInfo[ACTLibId].SegSize - Size);
 	this.SpriteRes[ACTFrameID].Sprite.recycle();
 	//[OTHER] end local v0           #Size:I
 	// :cond_2
 	}
 	this.SpriteRes[ACTFrameID].Sprite = null /* 0 */;
 	this.SpriteRes[ACTFrameID].SpriteResID = -1;
 	this.SpriteResACTInfo[ACTFrameID].RESACTIdx = -1;
 	// :cond_3
 	}
 	if (this.mIsLogOut) {
 	android.util.Log.v("GameEngine",new StringBuilder("Picture use RAM: ").append((this.nCurBMPRamSize / 1024)).append(" KBytes").toString());
 		}
 		}
 	// goto :goto_0
 	// :goto_0
 	return;
 }
 
 public void FreeAllACT(){
 	int i = 0;
 	while (i < this.nMaxSpriteResNum) {
 	if (this.SpriteRes[i].Sprite != null) {
 	this.SpriteRes[i].Sprite.recycle();
 	// :cond_1
 	}
 	this.SpriteRes[i].Sprite = null /* 0 */;
 	this.SpriteRes[i].SpriteResID = -1;
 	this.SpriteResACTInfo[i].RESACTIdx = -1;
 	this.pSpriteResSegInfo[i].SegSize = 0 /* 0 */;
 	this.pSpriteResSegInfo[i].SegIdx = -1;
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_0
 	}
 	this.nCurBMPRamSize = 0 /* 0 */;
 	System.gc();
 	return;
 }
 
 public int GetACTCount(int ACTId,int ACTFileName){
 	ACTId = (ACTId + this.mACTLibBeg);
 	;
 	oms.GameEngine.RESApp rs = new oms.GameEngine.RESApp(this.mContext);
 	rs.OpenRes(ACTFileName);
 	this.GetFileHead(rs,ACTId);
 	rs.CloseRes();
 	;
 	return this.ACTStructInfo.FileNum;
 	//return this.ACTStructInfo.FileNum;
 }
 
 public long GetBMPRamSize(){
 	;
 	return this.nCurBMPRamSize;
 	//return this.nCurBMPRamSize;
 }
 
 public android.graphics.Bitmap GetSpriteBitmap(int resId){
 	int SpriteIdx = -1;
 	if (resId < 0x7f02) {
 	;
 	return null /* 0 */;
 	} else {
 	SpriteIdx = (resId & 65535);
 	;
 	return this.SpriteRes[SpriteIdx].Sprite;
 		}
 	// goto :goto_0
 	// :goto_0
 	//return 0;
 }
 
 public long GetSpriteSegSize(int ACTLibIdx){
 	if (ACTLibIdx >= this.nMaxSpriteResNum) {
 	;
 	return 0 /* 0 */;
 	} else {
 	;
 	return (long)this.pSpriteResSegInfo[ACTLibIdx].SegSize;
 		}
 	// goto :goto_0
 	// :goto_0
 	//return 0;
 }
 
 public int GetSpriteXHitL(int SpriteID){
 	int SpriteIdx = -1;
 	if (this.SpriteResACTInfo[(SpriteID & 65535)].RESACTIdx != -1) {
 	SpriteIdx = (SpriteID & 65535);
 	;
 	return this.SpriteRes[SpriteIdx].SpriteXHitL;
 	} else {
 	;
 	return 0 /* 0 */;
 		}
 	// goto :goto_0
 	// :goto_0
 	//return this.SpriteRes[SpriteIdx].SpriteXHitL;
 }
 
 public int GetSpriteXHitR(int SpriteID){
 	int SpriteIdx = -1;
 	if (this.SpriteResACTInfo[(SpriteID & 65535)].RESACTIdx != -1) {
 	SpriteIdx = (SpriteID & 65535);
 	;
 	return this.SpriteRes[SpriteIdx].SpriteXHitR;
 	} else {
 	;
 	return 0 /* 0 */;
 		}
 	// goto :goto_0
 	// :goto_0
 	//return this.SpriteRes[SpriteIdx].SpriteXHitR;
 }
 
 public int GetSpriteYHitD(int SpriteID){
 	int SpriteIdx = -1;
 	if (this.SpriteResACTInfo[(SpriteID & 65535)].RESACTIdx != -1) {
 	SpriteIdx = (SpriteID & 65535);
 	;
 	return this.SpriteRes[SpriteIdx].SpriteYHitD;
 	} else {
 	;
 	return 0 /* 0 */;
 		}
 	// goto :goto_0
 	// :goto_0
 	//return this.SpriteRes[SpriteIdx].SpriteYHitD;
 }
 
 public int GetSpriteYHitU(int SpriteID){
 	int SpriteIdx = -1;
 	if (this.SpriteResACTInfo[(SpriteID & 65535)].RESACTIdx != -1) {
 	SpriteIdx = (SpriteID & 65535);
 	;
 	return this.SpriteRes[SpriteIdx].SpriteYHitU;
 	} else {
 	;
 	return 0 /* 0 */;
 		}
 	// goto :goto_0
 	// :goto_0
 	//return this.SpriteRes[SpriteIdx].SpriteYHitU;
 }
 
 public int GetSpriteZHitB(int SpriteID){
 	int SpriteIdx = -1;
 	if (this.SpriteResACTInfo[(SpriteID & 65535)].RESACTIdx != -1) {
 	SpriteIdx = (SpriteID & 65535);
 	;
 	return this.SpriteRes[SpriteIdx].SpriteZHitB;
 	} else {
 	;
 	return 0 /* 0 */;
 		}
 	// goto :goto_0
 	// :goto_0
 	//return this.SpriteRes[SpriteIdx].SpriteZHitB;
 }
 
 public int GetSpriteZHitF(int SpriteID){
 	int SpriteIdx = -1;
 	if (this.SpriteResACTInfo[(SpriteID & 65535)].RESACTIdx != -1) {
 	SpriteIdx = (SpriteID & 65535);
 	;
 	return this.SpriteRes[SpriteIdx].SpriteZHitF;
 	} else {
 	;
 	return 0 /* 0 */;
 		}
 	// goto :goto_0
 	// :goto_0
 	//return this.SpriteRes[SpriteIdx].SpriteZHitF;
 }
 
 	public void InitACT(int ACTLibId, int ACTFileName) {
 		ACTLibId = (ACTLibId + this.mACTLibBeg);
 		if (ACTLibId >= this.nMaxSpriteResNum) {
 			// :cond_0
 		} else {
 			if (this.pSpriteResSegInfo[ACTLibId].SegIdx != ACTLibId) {
 				this.InitSpriteResInfo(ACTFileName, ACTLibId);
 				this.CloseSpriteACTFile();
 				this.pSpriteResSegInfo[ACTLibId].SegIdx = (short) ACTLibId;
 			}
 		}
 		// goto :goto_0
 		// :goto_0
 		return;
 	}
 
 public void InitSprite(int SpriteNum){
 	this.nShowSpriteNum = 0 /* 0 */;
 	this.nMaxSpriteNum = SpriteNum;
 	if (this.nMaxSpriteNum == 0) {
 	this.nMaxSpriteNum = 1;
 	// :cond_0
 	}
 	;
 	this.Sprite = new oms.GameEngine.SpriteDEF[this.nMaxSpriteNum];
 	int i = 0;
 	while (i < this.nMaxSpriteNum) {
 	;
 	this.Sprite[i] = new oms.GameEngine.SpriteDEF();
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_1
 	}
 	return;
 }
 
 public void InitSpriteACTFile(){
 	;
 	this.cSpriteACTFile = new oms.GameEngine.SpriteManager.SpriteACTFILE();
 	return;
 }
 
 public void InitSpriteRes(int SpriteResNum){
 	this.nMaxSpriteResNum = SpriteResNum;
 	if (SpriteResNum <= 0) {
 	this.nMaxSpriteResNum = 1;
 	// :cond_0
 	}
 	;
 	this.SpriteRes = new oms.GameEngine.SpriteResDEF[this.nMaxSpriteResNum];
 	;
 	this.SpriteResACTInfo = new oms.GameEngine.SpriteManager.SpriteRESACTINFO[this.nMaxSpriteResNum];
 	int i = 0;
 	while (i < this.nMaxSpriteResNum) {
 	;
 	this.SpriteRes[i] = new oms.GameEngine.SpriteResDEF();
 	;
 	this.SpriteResACTInfo[i] = new oms.GameEngine.SpriteManager.SpriteRESACTINFO();
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_1
 	}
 	;
 	this.pSpriteResSegInfo = new oms.GameEngine.SpriteResSeg[this.nMaxSpriteResNum];
 	i = 0 /* 0 */;
 	while (i < this.nMaxSpriteResNum) {
 	;
 	this.pSpriteResSegInfo[i] = new oms.GameEngine.SpriteResSeg();
 	i = (i + 0x1);
 	// goto :goto_1
 	// :cond_2
 	}
 	;
 	this.ACTStructInfo = new oms.GameEngine.ACTStruct();
 	this.bLoadingSpriteRes = false /* 0 */;
 	this.nCurSpriteFileNum = 0 /* 0 */;
 	return;
 }
 
 public void InitSpriteResInfo(int ResId,int ACTLibId){
 	this.nCurSegLoadedSpriteNum = 0 /* 0 */;
 	if (!this.LoadSpriteACTFile(ResId)) {
 	return;
 	} else {
 	this.GetFileHead(ACTLibId);
 	int i = 0;
 	while (i < this.ACTStructInfo.FileNum) {
 	this.GetSpriteHead(i);
 	int j = 0;
 	while (j < this.ACTStructInfo.SpriteNum) {
 	int ACTFrameId = (this.GetSpriteResID(j) & 65535);
 	if (this.SpriteResACTInfo[ACTFrameId].RESACTIdx == -1) {
 	this.ReadSpriteResInfo(j,ACTLibId);
 	// :cond_3
 	}
 	j = (j + 0x1);
 	// goto :goto_1
 	// :cond_2
 	}
 	this.nCurSegLoadedSpriteNum = (this.nCurSegLoadedSpriteNum + this.ACTStructInfo.SpriteNum);
 	i = (i + 0x1);
 		}
 		}
 	// goto :goto_0
 }
 
 public void InitSpriteResInfo(int ResId,int ACTLibId,int ACTFrameId){
 	boolean ret = false;
 	if (!this.LoadSpriteACTFile(ResId)) {
 	return;
 	} else {
 	this.GetFileHead(ACTLibId);
 	int j = 0;
 	while (j < this.ACTStructInfo.FileNum) {
 	this.GetSpriteHead(j);
 	int i = 0;
 	while (i < this.ACTStructInfo.SpriteNum) {
 	if (this.GetSpriteResID(i) == ACTFrameId) {
 	ACTFrameId = (ACTFrameId & 65535);
 	if (this.SpriteResACTInfo[ACTFrameId].RESACTIdx == -1) {
 	this.ReadSpriteResInfo(i,ACTLibId);
 	// :cond_3
 	}
 	ret = true;
 	// goto :goto_2
 //break ; //
 break ; //
 	// :cond_4
 	}
 	i = (i + 0x1);
 	// goto :goto_1
 	// :cond_2
 	}
 	if (!ret) {
 	j = (j + 0x1);
 		}
 		}
 		}
 	// goto :goto_0
 }
 
 public void InitSpriteResInfo(int ResId,int ACTLibId,int ACTFrameBeg,int ACTFrameEnd){
 	if (!this.LoadSpriteACTFile(ResId)) {
 	return;
 	} else {
 	this.GetFileHead(ACTLibId);
 	this.GetSpriteHead(0);
 	int End;
 	if (ACTFrameEnd < this.ACTStructInfo.SpriteNum) {
 	End = ACTFrameEnd;
 	} else {
 	End = this.ACTStructInfo.SpriteNum;
 		}
 	// goto :goto_0
 	// :goto_0
 	int j = ACTFrameBeg;
 	while (j >= End) {
 	int ACTFrameId = (this.GetSpriteResID(j) & 65535);
 	if (this.SpriteResACTInfo[ACTFrameId].RESACTIdx == -1) {
 	this.ReadSpriteResInfo(j,ACTLibId);
 	// :cond_2
 	}
 	j = (j + 0x1);
 		}
 	// goto :goto_1
 	//[OTHER] end local v0           #ACTFrameId:I
 	//[OTHER] end local v1           #End:I
 	//[OTHER] end local v2           #j:I
 		}
 }
 
 public void LoadACT(int ACTLibId,int ACTFrameId,int ACTFileName){
 	ACTLibId = (ACTLibId + this.mACTLibBeg);
 	if (ACTLibId >= this.nMaxSpriteResNum) {
 	} else {
 	this.InitSpriteResInfo(ACTFileName,ACTLibId,ACTFrameId);
 	this.CloseSpriteACTFile();
 		}
 	// goto :goto_0
 	// :goto_0
 	return;
 }
 
 public void LoadACT(int ACTLibId,int ACTFrameBeg,int ACTFrameEnd,int ACTFileName){
 	ACTLibId = (ACTLibId + this.mACTLibBeg);
 	if (ACTLibId >= this.nMaxSpriteResNum) {
 	} else {
 	this.InitSpriteResInfo(ACTFileName,ACTLibId,ACTFrameBeg,ACTFrameEnd);
 	this.CloseSpriteACTFile();
 		}
 	// goto :goto_0
 	// :goto_0
 	return;
 }
 
 public boolean LoadACT(int ACTLibId,int ACTFileName){
 	boolean result = false;
 	ACTLibId = (ACTLibId + this.mACTLibBeg);
 	if (this.pSpriteResSegInfo[ACTLibId].SegIdx == ACTLibId) {
 	;
 	return true /* 1 */;
 	} else {
 	result = this.LoadSpriteResInfo(ACTFileName,ACTLibId);
 	if (result) {
 	this.CloseSpriteACTFile();
 	this.pSpriteResSegInfo[ACTLibId].SegIdx = (short)ACTLibId;
 	// :cond_1
 	}
 	;
 	return result;
 		}
 	// goto :goto_0
 	// :goto_0
 	//return 1;
 }
 
 public boolean LoadSpriteACTFile(int ResId){
 	;
 	oms.GameEngine.RESApp rs = new oms.GameEngine.RESApp(this.mContext);
 	rs.OpenRes(ResId);
 	if (rs.getLength() > 0) {
 	this.cSpriteACTFile.nACTBuffLen = rs.getLength();
 	;
 	this.cSpriteACTFile.pACTBuff = new byte[this.cSpriteACTFile.nACTBuffLen];
 	rs.ResRead(this.cSpriteACTFile.pACTBuff,0,this.cSpriteACTFile.nACTBuffLen);
 	rs.CloseRes();
 	;
 	return true /* 0 */;
 	} else {
 	rs.CloseRes();
 	;
 	return false /* 0 */;
 		}
 	// goto :goto_0
 	// :goto_0
 	//return 0;
 }
 
 public boolean LoadSpriteResInfo(int ResId,int ACTLibId){
 	boolean result = false;
 	if (!this.bLoadingSpriteRes) {
 	if (ACTLibId >= this.nMaxSpriteResNum) {
 	;
 	return true /* 1 */;
 	// :cond_0
 	} else if (!this.LoadSpriteACTFile(ResId)) {
 	;
 	return true /* 1 */;
 	// goto :goto_0
 	} else {
 	this.GetFileHead(ACTLibId);
 	this.nCurSegLoadedSpriteNum = 0 /* 0 */;
 	this.nCurSpriteFileNum = 0 /* 0 */;
 	this.bLoadingSpriteRes = true;
 		}
 	// :cond_2
 	}
 	if (this.nCurSpriteFileNum < this.ACTStructInfo.FileNum) {
 	this.GetSpriteHead(this.nCurSpriteFileNum);
 	int j = 0;
 	while (j < this.ACTStructInfo.SpriteNum) {
 	int ACTFrameId = (this.GetSpriteResID(j) & 65535);
 	if (this.SpriteResACTInfo[ACTFrameId].RESACTIdx == -1) {
 	this.ReadSpriteResInfo(j,ACTLibId);
 	// :cond_4
 	}
 	j = (j + 0x1);
 	// goto :goto_1
 	// :cond_3
 	}
 	this.nCurSegLoadedSpriteNum = (this.nCurSegLoadedSpriteNum + this.ACTStructInfo.SpriteNum);
 	this.nCurSpriteFileNum = (this.nCurSpriteFileNum + 0x1);
 	//[OTHER] end local v1           #j:I
 	} else {
 	this.bLoadingSpriteRes = false /* 0 */;
 	result = true;
 		}
 	// goto :goto_2
 	// :goto_2
 	;
 	return result;
 	// goto :goto_0
 	// :goto_0
 	//return 1;
 	
 	//[OTHER] end local v0           #ACTFrameId:I
 	//[OTHER] end local v1           #j:I
 }
 
 	public boolean OnDraw(android.graphics.Canvas canvas, int Attrib, int XOff,
 			int YOff, android.graphics.Paint[] paint) {
 		if ((Attrib == 0 || this.nCurFlushSpriteNum >= this.nShowSpriteNum)) {
 			// :cond_0
 			this.nCurFlushSpriteNum = 0 /* 0 */;
 			// :cond_1
 		}
 		int SpriteXVal = 0;
 		int SpriteYVal = 0;
 		if (this.nCurFlushSpriteNum < this.nShowSpriteNum) {
 			int i = 0;
 			while (i < this.nShowSpriteNum) {
 				int SpriteIdx = this.Sprite[i].SpriteResID;
 				if (SpriteIdx != -1 && this.SpriteRes[SpriteIdx].Sprite != null
 						&& this.Sprite[i].SpriteAttrib == Attrib) {
 					int XVal = 0;
 					int YVal = 0;
 					switch (this.nScreenOrientation) {
 					case 0:
 						SpriteXVal = (this.Sprite[i].SpriteXVal + XOff);
 						SpriteYVal = (this.Sprite[i].SpriteYVal + YOff);
 						break;
 					case 1:
 						SpriteXVal = ((oms.GameEngine.GameCanvas
 								.GetScreenWidth() - this.Sprite[i].SpriteYVal) + XOff);
 						SpriteYVal = (this.Sprite[i].SpriteXVal + YOff);
 						break;
 					case 2:
 						SpriteXVal = ((oms.GameEngine.GameCanvas
 								.GetScreenHeight() - this.Sprite[i].SpriteYVal) + XOff);
 						SpriteYVal = (this.Sprite[i].SpriteXVal + YOff);
 						break;
 					default:
 						SpriteXVal = ((oms.GameEngine.GameCanvas
 								.GetScreenHeight() - this.Sprite[i].SpriteYVal) + XOff);
 						SpriteYVal = ((oms.GameEngine.GameCanvas
 								.GetScreenWidth() - this.Sprite[i].SpriteXVal) + YOff);
 					} // end of switch
 
 					if ((SpriteYVal >= this.nViewRc.top || SpriteYVal <= this.nViewRc.bottom)
 							&& (SpriteXVal >= this.nViewRc.left || SpriteXVal <= this.nViewRc.right)) {
 						// :cond_4
 						// :cond_5
 						paint[this.Sprite[i].mPaintId]
 								.setAlpha((this.Sprite[i].Alpha & 0xff));
 
 						int saveCount = (this.Sprite[i].mTransform != 0) ? canvas
 								.save(1) : 0;
 						int flipX, flipY;
 						if ((this.Sprite[i].mTransform & 0x1) != 0) {
 							flipX = -1;
 							// (this.Sprite[i].mTransform & 0x1) =
 							// -SpriteXVal;
 							XVal = (-(this.Sprite[i].mTransform & 0x1) - this.Sprite[i].SpriteCenterX);
 						} else {
 							flipX = 1;
 
 							XVal = (SpriteXVal - this.Sprite[i].SpriteCenterX);
 						}
 
 						if ((this.Sprite[i].mTransform & 0x2) != 0) {
 							flipY = -1;
 							// (this.Sprite[i].mTransform & 0x2) =
 							// -SpriteYVal;
 							YVal = (-(this.Sprite[i].mTransform & 0x2) - this.Sprite[i].SpriteCenterY);
 						} else {
 							flipY = 1;
 
 							YVal = (SpriteYVal - this.Sprite[i].SpriteCenterY);
 						}
 						if (this.Sprite[i].mTransform != 0) {
 							canvas.scale((float) flipX, (float) flipY);
 							// :cond_6
 						}
 
 						if ((this.Sprite[i].Rotate - 0) == 0
 								&& (this.Sprite[i].ScaleX - 1.0F) == 0
 								&& (this.Sprite[i].ScaleY - 1.0F) == 0) {
 							canvas.drawBitmap(this.SpriteRes[SpriteIdx].Sprite,
 									(float) XVal, (float) YVal,
 									paint[this.Sprite[i].mPaintId]);
 						} else {
 							// :cond_c
 							this.cMatrix.setTranslate((float) XVal,
 									(float) YVal);
 							this.cMatrix.postScale(this.Sprite[i].ScaleX,
 									this.Sprite[i].ScaleY, (float) SpriteXVal,
 									(float) SpriteYVal);
 							if (this.Sprite[i].RotateX != -1
 									&& this.Sprite[i].RotateY != -1) {
 								this.cMatrix.postRotate(this.Sprite[i].Rotate,
 										(float) this.Sprite[i].RotateX,
 										(float) this.Sprite[i].RotateY);
 							} else {
 								this.cMatrix.postRotate(this.Sprite[i].Rotate,
 										(float) SpriteXVal, (float) SpriteYVal);
 							}
 							canvas.drawBitmap(this.SpriteRes[SpriteIdx].Sprite,
 									this.cMatrix,
 									paint[this.Sprite[i].mPaintId]);
 						}
 						if (this.Sprite[i].mTransform != 0) {
 							canvas.restoreToCount(saveCount);
 							// :cond_7
 						}
 						this.nCurFlushSpriteNum = (this.nCurFlushSpriteNum + 0x1);
 						// [OTHER] end local v8 #XVal:I
 						// [OTHER] end local v9 #YVal:I
 						// [OTHER] end local v10 #flipX:I
 						// [OTHER] end local v11 #flipY:I
 						// [OTHER] end local v13
 						// #saveCount:I
 						// :cond_8
 					}
 				}
 				// cond 8
 				if (this.nCurFlushSpriteNum == this.nShowSpriteNum) {
 					;
 					return true;
 				}
 
 				i++;
 
 			}
 		}
 		return false;
 	}
 
 public boolean OnDraw(android.graphics.Canvas canvas,int Attrib,android.graphics.Paint paint){
 	if (Attrib == 0) {
 	this.nCurFlushSpriteNum = 0 /* 0 */;
 	// :cond_0
 	}
 	int SpriteXVal = 0;
 	int SpriteYVal = 0;
 	if (this.nCurFlushSpriteNum < this.nShowSpriteNum) {
 	int i = 0;
 	while (i < this.nShowSpriteNum) {
 	int SpriteIdx = this.Sprite[i].SpriteResID;
 	if (SpriteIdx != -1 && this.SpriteRes[SpriteIdx].Sprite != null && this.Sprite[i].SpriteAttrib == Attrib) {
 	;
 	switch(this.nScreenOrientation){
 	case 0: 
 	break;
 	case 1: 
 	break;
 	case 2: 
 	break;
 	default:
 	SpriteXVal = (oms.GameEngine.GameCanvas.GetScreenHeight() - this.Sprite[i].SpriteYVal);
 	SpriteYVal = (oms.GameEngine.GameCanvas.GetScreenWidth() - this.Sprite[i].SpriteXVal);
 	} //end of switch
 	//:pswitch_0
 	if ((SpriteYVal >= this.nViewRc.top || SpriteYVal <= this.nViewRc.bottom) && (SpriteXVal >= this.nViewRc.left || SpriteXVal <= this.nViewRc.right)) {
 	// :cond_3
 	// :cond_4
 	canvas.drawBitmap(this.SpriteRes[SpriteIdx].Sprite,(float)(SpriteXVal - this.Sprite[i].SpriteCenterX),(float)(SpriteYVal - this.Sprite[i].SpriteCenterY),paint);
 	this.nCurFlushSpriteNum = (this.nCurFlushSpriteNum + 0x1);
 	// :cond_5
 	}
 	}
 	if (this.nCurFlushSpriteNum == this.nShowSpriteNum) {
 	;
 	return true /* 1 */;
 	//goto somewhere; //maybe return,continue,break: goto :goto_1
 	// :cond_6
 	}
 	i = (i + 0x1);
 	// goto/16 :goto_0
 	// :cond_2
 	}
 	//[OTHER] end local v3           #i:I
 	// :cond_1
 	}
 	;
 	return false /* 0 */;
 	//return 0;
 	
 	
 }
 
 public void SetACTLibBeg(int beg){
 	this.mACTLibBeg = beg;
 	return;
 }
 
 public void SetBMPSizeOut(boolean out){
 	this.mIsLogOut = out;
 	return;
 }
 
 public void SetPaintId(int spriteId,int paintId){
 	this.Sprite[spriteId].mPaintId = paintId;
 	return;
 }
 
 public void SetScreenOrientation(int orientation){
 	this.nScreenOrientation = orientation;
 	return;
 }
 
 public void SetSpriteAlpha(int spriteId,short alpha){
 	this.Sprite[spriteId].Alpha = alpha;
 	return;
 }
 
 public void SetSpriteTransform(int spriteId,int transform){
 	this.Sprite[spriteId].mTransform = transform;
 	return;
 }
 
 	public int WriteSprite(int SpriteResId, int SpriteX, int SpriteY,
 			int SpriteAttr) {
 		int SpriteIdx = -1;
 		if (this.nShowSpriteNum >= this.nMaxSpriteNum) {
 			// [OTHER] end local v0 #SpriteIdx:I
 			// int SpriteIdx = SpriteIdx;
 			return SpriteIdx;
 			// :cond_0
 		} else if (SpriteResId < 0x7f02) {
 			// SpriteIdx = SpriteIdx;
 			// [OTHER] end local v0 #SpriteIdx:I
 			;
 			return SpriteIdx;
 			// goto :goto_0
 		} else {
 			if (this.SpriteResACTInfo[(SpriteResId & 0xffff)].RESACTIdx != -1) {
 				SpriteIdx = (SpriteResId & 0xffff);
 				// :cond_2
 			}
 			if (SpriteIdx != -1) {
 				this.Sprite[this.nShowSpriteNum].SpriteResID = (short) SpriteIdx;
 				this.Sprite[this.nShowSpriteNum].SpriteXVal = SpriteX;
 				this.Sprite[this.nShowSpriteNum].SpriteYVal = SpriteY;
 				this.Sprite[this.nShowSpriteNum].Rotate = 0 /* 0 */;
 				this.Sprite[this.nShowSpriteNum].ScaleX = 1.0F;
 				this.Sprite[this.nShowSpriteNum].ScaleY = 1.0F;
 				this.Sprite[this.nShowSpriteNum].Alpha = 255;
 				this.Sprite[this.nShowSpriteNum].mPaintId = 0 /* 0 */;
 				this.Sprite[this.nShowSpriteNum].mTransform = 0 /* 0 */;
 				this.Sprite[this.nShowSpriteNum].SpriteCenterX = (short) this.SpriteRes[SpriteIdx].SpriteCenterX;
 				this.Sprite[this.nShowSpriteNum].SpriteCenterY = (short) this.SpriteRes[SpriteIdx].SpriteCenterY;
 				this.Sprite[this.nShowSpriteNum].RotateX = -1;
 				this.Sprite[this.nShowSpriteNum].RotateY = -1;
 				this.Sprite[this.nShowSpriteNum].SpriteAttrib = SpriteAttr;
				this.nShowSpriteNum = (this.nShowSpriteNum + 0x1);
 				SpriteIdx = this.nShowSpriteNum;
 				// :cond_3
 			}
 			// [OTHER] end local v0 #SpriteIdx:I
 			;
 			return SpriteIdx;
 		}
 		// goto/16 :goto_0
 		// [OTHER] end local v1 #SpriteIdx:I
 
 		// :goto_0
 		// return SpriteIdx;
 		// [OTHER] end local v1 #SpriteIdx:I
 
 	}
 
 	public int WriteSprite(int SpriteResId, int SpriteX, int SpriteY,
 			int SpriteAttr, float rotate, float scale) {
 		int SpriteIdx = -1;
 		if (this.nShowSpriteNum >= this.nMaxSpriteNum) {
 			// [OTHER] end local v0 #SpriteIdx:I
 			// int SpriteIdx = SpriteIdx;
 			return SpriteIdx;
 			// :cond_0
 		} else if (SpriteResId < 0x7f02) {
 			// SpriteIdx = SpriteIdx;
 			// [OTHER] end local v0 #SpriteIdx:I
 			;
 			return SpriteIdx;
 			// goto :goto_0
 		} else {
 			if (this.SpriteResACTInfo[(SpriteResId & 65535)].RESACTIdx != -1) {
 				SpriteIdx = (SpriteResId & 65535);
 				// :cond_2
 			}
 			if (SpriteIdx != -1) {
 				this.Sprite[this.nShowSpriteNum].SpriteResID = (short) SpriteIdx;
 				this.Sprite[this.nShowSpriteNum].SpriteXVal = SpriteX;
 				this.Sprite[this.nShowSpriteNum].SpriteYVal = SpriteY;
 				this.Sprite[this.nShowSpriteNum].Rotate = rotate;
 				this.Sprite[this.nShowSpriteNum].ScaleX = scale;
 				this.Sprite[this.nShowSpriteNum].ScaleY = scale;
 				this.Sprite[this.nShowSpriteNum].Alpha = 255;
 				this.Sprite[this.nShowSpriteNum].mTransform = 0 /* 0 */;
 				this.Sprite[this.nShowSpriteNum].SpriteCenterX = (short) this.SpriteRes[SpriteIdx].SpriteCenterX;
 				this.Sprite[this.nShowSpriteNum].SpriteCenterY = (short) this.SpriteRes[SpriteIdx].SpriteCenterY;
 				this.Sprite[this.nShowSpriteNum].RotateX = -1;
 				this.Sprite[this.nShowSpriteNum].RotateY = -1;
 				this.Sprite[this.nShowSpriteNum].SpriteAttrib = SpriteAttr;
				this.nShowSpriteNum = (this.nShowSpriteNum + 0x1);
 				SpriteIdx = this.nShowSpriteNum;
 				// :cond_3
 			}
 			// [OTHER] end local v0 #SpriteIdx:I
 			;
 			return SpriteIdx;
 		}
 		// goto/16 :goto_0
 		// [OTHER] end local v1 #SpriteIdx:I
 
 		// :goto_0
 		// return SpriteIdx;
 		// [OTHER] end local v1 #SpriteIdx:I
 
 	}
 
 	public int WriteSprite(int SpriteResId, int SpriteX, int SpriteY,
 			int SpriteAttr, float rotate, float scaleX, float scaleY,
 			short RotateX, short RotateY) {
 		int SpriteIdx = -1;
 		if (this.nShowSpriteNum >= this.nMaxSpriteNum) {
 			// [OTHER] end local v0 #SpriteIdx:I
 			// int SpriteIdx = SpriteIdx;
 			return SpriteIdx;
 			// :cond_0
 		} else if (SpriteResId < 0x7f02) {
 			// SpriteIdx = SpriteIdx;
 			// [OTHER] end local v0 #SpriteIdx:I
 			;
 			return SpriteIdx;
 			// goto :goto_0
 		} else {
 			if (this.SpriteResACTInfo[(65535 & SpriteResId)].RESACTIdx != -1) {
 				SpriteIdx = (SpriteResId & 65535);
 				// :cond_2
 			}
 			if (SpriteIdx != -1) {
 				this.Sprite[this.nShowSpriteNum].SpriteResID = (short) SpriteIdx;
 				this.Sprite[this.nShowSpriteNum].SpriteXVal = SpriteX;
 				this.Sprite[this.nShowSpriteNum].SpriteYVal = SpriteY;
 				this.Sprite[this.nShowSpriteNum].Rotate = rotate;
 				this.Sprite[this.nShowSpriteNum].ScaleX = scaleX;
 				this.Sprite[this.nShowSpriteNum].ScaleY = scaleY;
 				this.Sprite[this.nShowSpriteNum].Alpha = 255;
 				this.Sprite[this.nShowSpriteNum].mPaintId = 0 /* 0 */;
 				this.Sprite[this.nShowSpriteNum].mTransform = 0 /* 0 */;
 				this.Sprite[this.nShowSpriteNum].SpriteCenterX = (short) this.SpriteRes[SpriteIdx].SpriteCenterX;
 				this.Sprite[this.nShowSpriteNum].SpriteCenterY = (short) this.SpriteRes[SpriteIdx].SpriteCenterY;
 				if (RotateX != 65535) {
 					this.Sprite[this.nShowSpriteNum].RotateX = RotateX;
 				} else {
 					this.Sprite[this.nShowSpriteNum].RotateX = this.Sprite[this.nShowSpriteNum].SpriteCenterX;
 				}
 				// goto :goto_1
 				// :goto_1
 				if (RotateX != 65535) {
 					this.Sprite[this.nShowSpriteNum].RotateY = RotateY;
 				} else {
 					this.Sprite[this.nShowSpriteNum].RotateY = this.Sprite[this.nShowSpriteNum].SpriteCenterY;
 				}
 				// goto :goto_2
 				// :goto_2
 				this.Sprite[this.nShowSpriteNum].SpriteAttrib = SpriteAttr;
				this.nShowSpriteNum = (this.nShowSpriteNum + 0x1);
 				SpriteIdx = this.nShowSpriteNum;
 				// :cond_3
 			}
 			// [OTHER] end local v0 #SpriteIdx:I
 			;
 			return SpriteIdx;
 		}
 		// goto/16 :goto_0
 		// [OTHER] end local v1 #SpriteIdx:I
 
 		// [OTHER] end local v1 #SpriteIdx:I
 
 		// :goto_0
 		// return SpriteIdx;
 		// [OTHER] end local v1 #SpriteIdx:I
 
 	}
 
 	public int WriteSprite(int SpriteResId, int SpriteX, int SpriteY,
 			int SpriteAttr, float rotate, float scale, short RotateX,
 			short RotateY) {
 		int SpriteIdx = -1;
 		if (this.nShowSpriteNum >= this.nMaxSpriteNum) {
 			// [OTHER] end local v0 #SpriteIdx:I
 			// int SpriteIdx = SpriteIdx;
 			return SpriteIdx;
 			// :cond_0
 		} else if (SpriteResId < 0x7f02) {
 			// SpriteIdx = SpriteIdx;
 			// [OTHER] end local v0 #SpriteIdx:I
 			;
 			return SpriteIdx;
 			// goto :goto_0
 		} else {
 			if (this.SpriteResACTInfo[(SpriteResId & 65535)].RESACTIdx != -1) {
 				SpriteIdx = (SpriteResId & 65535);
 				// :cond_2
 			}
 			if (SpriteIdx != -1) {
 				this.Sprite[this.nShowSpriteNum].SpriteResID = (short) SpriteIdx;
 				this.Sprite[this.nShowSpriteNum].SpriteXVal = SpriteX;
 				this.Sprite[this.nShowSpriteNum].SpriteYVal = SpriteY;
 				this.Sprite[this.nShowSpriteNum].Rotate = rotate;
 				this.Sprite[this.nShowSpriteNum].ScaleX = scale;
 				this.Sprite[this.nShowSpriteNum].ScaleY = scale;
 				this.Sprite[this.nShowSpriteNum].Alpha = 255;
 				this.Sprite[this.nShowSpriteNum].mPaintId = 0 /* 0 */;
 				this.Sprite[this.nShowSpriteNum].mTransform = 0 /* 0 */;
 				this.Sprite[this.nShowSpriteNum].SpriteCenterX = (short) this.SpriteRes[SpriteIdx].SpriteCenterX;
 				this.Sprite[this.nShowSpriteNum].SpriteCenterY = (short) this.SpriteRes[SpriteIdx].SpriteCenterY;
 				if (RotateX != 65535) {
 					this.Sprite[this.nShowSpriteNum].RotateX = RotateX;
 				} else {
 					this.Sprite[this.nShowSpriteNum].RotateX = this.Sprite[this.nShowSpriteNum].SpriteCenterX;
 				}
 				// goto :goto_1
 				// :goto_1
 				if (RotateX != 65535) {
 					this.Sprite[this.nShowSpriteNum].RotateY = RotateY;
 				} else {
 					this.Sprite[this.nShowSpriteNum].RotateY = this.Sprite[this.nShowSpriteNum].SpriteCenterY;
 				}
 				// goto :goto_2
 				// :goto_2
 				this.Sprite[this.nShowSpriteNum].SpriteAttrib = SpriteAttr;
				this.nShowSpriteNum = (this.nShowSpriteNum + 0x1);
 				SpriteIdx = this.nShowSpriteNum;
 				// :cond_3
 			}
 			// [OTHER] end local v0 #SpriteIdx:I
 			;
 			return SpriteIdx;
 		}
 		// goto/16 :goto_0
 		// [OTHER] end local v1 #SpriteIdx:I
 
 		// [OTHER] end local v1 #SpriteIdx:I
 
 		// :goto_0
 		// return SpriteIdx;
 		// [OTHER] end local v1 #SpriteIdx:I
 
 	}
 
 public void release(){
 	if (this.SpriteRes != null) {
 	int i = 0;
 	while (i < this.nMaxSpriteResNum) {
 	if (this.SpriteRes[i] != null) {
 	this.SpriteRes[i].release();
 	// :cond_3
 	}
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_2
 	}
 	//[OTHER] end local v0           #i:I
 	// :cond_0
 	}
 	this.SpriteRes = null /* 0 */;
 	this.nMaxSpriteResNum = 0 /* 0 */;
 	this.SpriteResACTInfo = null /* 0 */;
 	this.nCurSpriteFileNum = 0 /* 0 */;
 	this.bLoadingSpriteRes = false /* 0 */;
 	this.pSpriteResSegInfo = null /* 0 */;
 	this.nCurSegLoadedSpriteNum = 0 /* 0 */;
 	this.ACTStructInfo = null /* 0 */;
 	if (this.Sprite != null) {
 	int i = 0 /* 0 */;
 	
 	while (i < this.nMaxSpriteNum) {
 	this.Sprite[i].release();
 	i = (i + 0x1);
 	// goto :goto_1
 	// :cond_4
 	}
 	//[OTHER] end local v0           #i:I
 	// :cond_1
 	}
 	this.Sprite = null /* 0 */;
 	this.nMaxSpriteNum = 0 /* 0 */;
 	this.nShowSpriteNum = 0 /* 0 */;
 	this.CloseSpriteACTFile();
 	return;
 	
 }
 
 
 
 
 
 
 
 
 
 
 class SpriteACTFILE extends Object  {
 
 	// annotations
 	// value = Loms/GameEngine/SpriteManager;
 	// accessFlags = 0xa
 	// name = "SpriteACTFILE"
 	// instance fields
 	int nACTBuffLen;
 	byte[] pACTBuff;
 	// direct methods
 
 	public SpriteACTFILE(){
 		super();
 		this.pACTBuff = null /* 0 */;
 		this.nACTBuffLen = 0 /* 0 */;
 	}
 	// virtual methods
 
 	public void release(){
 		if (this.pACTBuff != null) {
 		this.pACTBuff = null /* 0 */;
 		// :cond_0
 		}
 		this.nACTBuffLen = 0 /* 0 */;
 		return;
 	}
 
 }
 
 
 
 class SpriteRESACTINFO extends Object  {
 
 // annotations
 // value = Loms/GameEngine/SpriteManager;
 // accessFlags = 0xa
 // name = "SpriteRESACTINFO"
 // instance fields
 short RESACTIdx;
 // direct methods
 
 public SpriteRESACTINFO(){
 	super();
 	this.RESACTIdx = -1;
 }
 
 }
 }

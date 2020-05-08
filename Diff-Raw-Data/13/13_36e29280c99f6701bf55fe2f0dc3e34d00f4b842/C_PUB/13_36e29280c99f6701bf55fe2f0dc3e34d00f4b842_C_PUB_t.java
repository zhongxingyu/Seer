 package info.pharos.ly;
 
 
 public class C_PUB extends Object  {
 
 // direct methods
 
 public C_PUB(){
 	super();
 }
 
 public static double AngleToRadian(double Angle){
 	 return 3.141592653589793D * Angle / 180.0D;
 	//return Radian;
 }
 
 	public static boolean CHKIsTouch(int SXHitL, int SXHitR, int SYHitU,
 			int SYHitD, int DACTName, int DXVal, int DYVal) {
 		SXHitL = 0 /* 0 */;
 		// [OTHER] end local p1
 		// [OTHER] end local p2
 		int i = SXHitL;
 		SXHitR = 4;
 		while (i < SXHitR) {
 			info.pharos.ly.C_Global.g_TouchFlag[i] = (info.pharos.ly.C_Global.g_TouchFlag[i] & 0x2);
 			SYHitU = 2;
 			if (info.pharos.ly.C_Global.g_TouchFlag[i] == SYHitU) {
 				int Touch_X = info.pharos.ly.C_Global.g_Touch_X[i];
 				int Touch_Y = info.pharos.ly.C_Global.g_Touch_Y[i];
 				// com.game.UnicornDash.C_OPhoneApp.cLib =
 				// com.game.UnicornDash.C_OPhoneApp.cLib.getGameCanvas().CHKACTTouch(Touch_X,Touch_Y,2,2,2,2,DACTName,DXVal,DYVal);
 				if (info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas()
 						.CHKACTTouch(Touch_X, Touch_Y, 2, 2, 2, 2, DACTName,
 								DXVal, DYVal)) {
 					// i = 1;
 					return true /* 1 */;
 				}
 				// goto :goto_1
 				// [OTHER] end local v1 #Touch_X:I
 				// [OTHER] end local v2 #Touch_Y:I
 				// :cond_1
 			}
 			i = (i + 0x1);
 			// goto :goto_0
 			// :cond_0
 		}
 		// i = 0 /* 0 */;
 		// [OTHER] end local p0 #i:I
 		return false;
 
 	}
 
 public static boolean CHKTouchDown(int SXHitL,int SXHitR,int SYHitU,int SYHitD,int DACTName,int DXVal,int DYVal){
 	//[OTHER] end local p0
 	//com.game.UnicornDash.C_OPhoneApp.cTouch = com.game.UnicornDash.C_OPhoneApp.cTouch.CHKTouchDown();
 	if (info.pharos.ly.C_OPhoneApp.cTouch.CHKTouchDown()) {
 	//com.game.UnicornDash.C_OPhoneApp.cTouch = null /* 0 */;
 	//[OTHER] end local p1
 	//[OTHER] end local p2
 	int i = 0;
 	SXHitR = 4;
 	while (i < SXHitR) {
 	//com.game.UnicornDash.C_OPhoneApp.cTouch = com.game.UnicornDash.C_OPhoneApp.cTouch.getTouchDownId(i);
 	SYHitU = -1;
 	if (info.pharos.ly.C_OPhoneApp.cTouch.getTouchDownId(i) != SYHitU) {
 	int Touch_X = info.pharos.ly.C_OPhoneApp.cTouch.getTouchDownX(i);
 	int Touch_Y = info.pharos.ly.C_OPhoneApp.cTouch.getTouchDownY(i);
 	//com.game.UnicornDash.C_OPhoneApp.cLib = com.game.UnicornDash.C_OPhoneApp.cLib.getGameCanvas().CHKACTTouch(Touch_X,Touch_Y,2,2,2,2,DACTName,DXVal,DYVal);
 	if (info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().CHKACTTouch(Touch_X,Touch_Y,2,2,2,2,DACTName,DXVal,DYVal)) {
 	i = 1;
 	return true /* 1 */;
 		}
 	// goto :goto_1
 	//[OTHER] end local v1           #Touch_X:I
 	//[OTHER] end local v2           #Touch_Y:I
 	// :cond_2
 	}
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_1
 	}
 	//[OTHER] end local p0           #i:I
 	// :cond_0
 	}
 	//i = 0 /* 0 */;
 	return false /* 0 */;
 	//return i;
 	
 }
 
 public static boolean CHKTouchUp(int SXHitL,int SXHitR,int SYHitU,int SYHitD,int DACTName,int DXVal,int DYVal){
 	//[OTHER] end local p0
 	//com.game.UnicornDash.C_OPhoneApp.cTouch = com.game.UnicornDash.C_OPhoneApp.cTouch.CHKTouchUp();
 	if (info.pharos.ly.C_OPhoneApp.cTouch.CHKTouchUp()) {
 	//com.game.UnicornDash.C_OPhoneApp.cTouch = null /* 0 */;
 	//[OTHER] end local p1
 	//[OTHER] end local p2
 	int i = 0;
 	SXHitR = 4;
 	while (i < SXHitR) {
 	//com.game.UnicornDash.C_OPhoneApp.cTouch = com.game.UnicornDash.C_OPhoneApp.cTouch.getTouchUpId(i);
 	SYHitU = -1;
 	if (info.pharos.ly.C_OPhoneApp.cTouch.getTouchUpId(i) != SYHitU) {
 	int Touch_X = info.pharos.ly.C_OPhoneApp.cTouch.getTouchUpX(i);
 	int Touch_Y = info.pharos.ly.C_OPhoneApp.cTouch.getTouchUpY(i);
 	//com.game.UnicornDash.C_OPhoneApp.cLib = com.game.UnicornDash.C_OPhoneApp.cLib.getGameCanvas().CHKACTTouch(Touch_X,Touch_Y,2,2,2,2,DACTName,DXVal,DYVal);
 	if (info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().CHKACTTouch(Touch_X,Touch_Y,2,2,2,2,DACTName,DXVal,DYVal)) {
 	i = 1;
 	return true /* 1 */;
 		}
 	// goto :goto_1
 	//[OTHER] end local v1           #Touch_X:I
 	//[OTHER] end local v2           #Touch_Y:I
 	// :cond_2
 	}
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_1
 	}
 	//[OTHER] end local p0           #i:I
 	// :cond_0
 	}
 //	i = 0 /* 0 */;
 	return false /* 0 */;
 	//return i;
 	
 }
 
 	public static void CHKTouch_Input() {
 		int i = 0;
 		while (i < 4) {
 			if ((info.pharos.ly.C_Global.g_TouchFlag[i] & 0x8) == 8) {
 				info.pharos.ly.C_Global.g_TouchId[i] = -1;
 				info.pharos.ly.C_Global.g_TouchFlag[i] = (info.pharos.ly.C_Global.g_TouchFlag[i] & -0x3);
 				// :cond_4
 			}
 			info.pharos.ly.C_Global.g_TouchFlag[i] = (info.pharos.ly.C_Global.g_TouchFlag[i] & -0x5);
 			info.pharos.ly.C_Global.g_TouchFlag[i] = (info.pharos.ly.C_Global.g_TouchFlag[i] & -0x9);
 			i = (i + 0x1);
 			// goto :goto_0
 			// :cond_3
 		}
 		if (info.pharos.ly.C_OPhoneApp.cTouch.CHKTouchDown()) {
 			i = 0 /* 0 */;
 			while (i < 4) {
 				if (info.pharos.ly.C_OPhoneApp.cTouch.getTouchDownId(i) != -1) {
 					info.pharos.ly.C_Global.g_TouchId[i] = 0 /* i */;
 					info.pharos.ly.C_Global.g_TouchFlag[i] = (info.pharos.ly.C_Global.g_TouchFlag[i] | 0x2);
 					info.pharos.ly.C_Global.g_TouchFlag[i] = (info.pharos.ly.C_Global.g_TouchFlag[i] | 0x4);
 					info.pharos.ly.C_Global.g_Touch_X[i] = info.pharos.ly.C_OPhoneApp.cTouch
 							.getTouchDownX(i);
 					info.pharos.ly.C_Global.g_Touch_Y[i] = info.pharos.ly.C_OPhoneApp.cTouch
 							.getTouchDownY(i);
 					// :cond_6
 				}
 				i = (i + 0x1);
 				// goto :goto_1
 				// :cond_5
 			}
 			// :cond_0
 		}
 		if (info.pharos.ly.C_OPhoneApp.cTouch.CHKTouchMove()) {
 			i = 0 /* 0 */;
 			while (i < 4) {
 				if (info.pharos.ly.C_OPhoneApp.cTouch.getTouchMoveId(i) != -1
 						&& info.pharos.ly.C_OPhoneApp.cTouch
 								.getTouchMoveCount(i) > 0) {
 					info.pharos.ly.C_Global.g_TouchId[i] = 0 /* i */;
 					info.pharos.ly.C_Global.g_TouchFlag[i] = (info.pharos.ly.C_Global.g_TouchFlag[i] | 0x2);
 					info.pharos.ly.C_Global.g_TouchFlag[i] = (info.pharos.ly.C_Global.g_TouchFlag[i] | 0x10);
 					info.pharos.ly.C_Global.g_Touch_X[i] = info.pharos.ly.C_OPhoneApp.cTouch
 							.getTouchMoveX(i,
 									(info.pharos.ly.C_OPhoneApp.cTouch
 											.getTouchMoveCount(i) - 1));
 					info.pharos.ly.C_Global.g_Touch_Y[i] = info.pharos.ly.C_OPhoneApp.cTouch
 							.getTouchMoveY(i,
 									(info.pharos.ly.C_OPhoneApp.cTouch
 											.getTouchMoveCount(i) - 1));
 					// :cond_8
 				}
 				i = (i + 0x1);
 				// goto/16 :goto_2
 				// :cond_7
 			}
 			// :cond_1
 		}
 		if (info.pharos.ly.C_OPhoneApp.cTouch.CHKTouchUp()) {
 			i = 0 /* 0 */;
 			while (i < 4) {
 				if (info.pharos.ly.C_OPhoneApp.cTouch.getTouchUpId(i) != -1) {
 					info.pharos.ly.C_Global.g_TouchId[i] = 0 /* i */;
 					info.pharos.ly.C_Global.g_TouchFlag[i] = (info.pharos.ly.C_Global.g_TouchFlag[i] | 0x2);
 					info.pharos.ly.C_Global.g_TouchFlag[i] = (info.pharos.ly.C_Global.g_TouchFlag[i] | 0x8);
 					info.pharos.ly.C_Global.g_Touch_X[i] = info.pharos.ly.C_OPhoneApp.cTouch
 							.getTouchUpX(i);
 					info.pharos.ly.C_Global.g_Touch_Y[i] = info.pharos.ly.C_OPhoneApp.cTouch
 							.getTouchUpY(i);
 					// :cond_a
 				}
 				i = (i + 0x1);
 				// goto/16 :goto_3
 				// :cond_9
 			}
 			// :cond_2
 		}
 		return;
 	}
 
 public static boolean CHKisTouch(){
 	int i = 0;
 	while (i < 4) {
 	if (info.pharos.ly.C_Global.g_TouchId[i] != -1) {
 	;
 	return true /* 1 */;
 	// goto :goto_1
 	} else {
 	i = (i + 0x1);
 		}
 	// goto :goto_0
 	// :cond_0
 	}
 	;
 	return false /* 0 */;
 	//return 0;
 }
 
 	public static int GetActPixel(int ACTName, int XVal, int YVal, int Tar_X,
 			int Tar_Y) {
 		android.graphics.Bitmap bmp = info.pharos.ly.C_OPhoneApp.cLib
 				.getGameCanvas().GetSpriteBitmap(ACTName);
 		int DYHitU = info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas()
 				.GetSpriteYHitU(ACTName);
 		int DXHitL = info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas()
 				.GetSpriteXHitL(ACTName);
 		int color = 0;
 		if (DYHitU != -1) {
 			color = 0 /* 0 */;
 			// [OTHER] end local v0 #color:I
 			// int color = color;
 		} else {
 			// color = color;
 			// [OTHER] end local v0 #color:I
 
 		}
 		// goto :goto_0
 		// :goto_0
 		// ACTName =
 		// com.game.UnicornDash.C_OPhoneApp.cLib.getGameCanvas().CHKACTTouch(Tar_X,Tar_Y,1,1,1,1,ACTName,XVal,YVal);
 		// [OTHER] end local p0
 		if (info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().CHKACTTouch(
 				Tar_X, Tar_Y, 1, 1, 1, 1, ACTName, XVal, YVal)) {
 			ACTName = (Tar_X - XVal);
 			ACTName = (ACTName + DXHitL);
 			ACTName = (ACTName + 0x1);
 			int x = ACTName;
 			XVal = (Tar_Y - YVal);
 			XVal = (XVal + DYHitU);
 			XVal = (XVal + 0x1);
 			int y = XVal;
 			x = bmp.getPixel(x, y);
 			// [OTHER] end local v13 #color:I
 			// [OTHER] end local p1 #y:I
 			// int color = x;
 			return x;
 		} else {
 			// color = color;
 			// [OTHER] end local v13 #color:I
 			;
 			return color;
 		}
 		// goto :goto_1
 		// :goto_1
 		// return color;
 		// [OTHER] end local p0 #color:I
 
 		// int XVal = y;
 
 		// int ACTName = color;
 	}
 
 public static void InitTouch(){
 	int i = 0;
 	while (i < 4) {
 	info.pharos.ly.C_Global.g_TouchId[i] = -1;
 	info.pharos.ly.C_Global.g_TouchFlag[i] = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_Touch_X[i] = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_Touch_Y[i] = 0 /* 0 */;
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_0
 	}
 	return;
 }
 
 public static double RadianToAngle(double Radian){
 	 return 180.0D * Radian / 3.141592653589793D;
 	//return Angle;
 }
 
 public static int Random(int Ran){
 	;
 	return (info.pharos.ly.C_Global.m_random.nextInt(Ran) % Ran);
 	//return (com.game.UnicornDash.C_Global.m_random.nextInt(Ran) % Ran);
 }
 
 public static void Rate(){
 	android.net.Uri uri = android.net.Uri.parse("market://details?id=com.RunnerGames.game.PumpkinsVsMonster_AD");
 	;
 	android.content.Intent it = new android.content.Intent("android.intent.action.VIEW",uri);
 	it.addFlags(0x10000000);
 	info.pharos.ly.C_OPhoneApp.cLib.GetActivity().startActivity(it);
 	return;
 }
 
 /*
 public static void ShowAdView(boolean Flag){
 	com.game.UnicornDash.UnicornDash activity = (com.game.UnicornDash.UnicornDash)com.game.UnicornDash.C_OPhoneApp.cLib.GetActivity();
 	if (activity != null) {
 	activity.setAdVisibility(Flag);
 	// :cond_0
 	}
 	return;
 }
  */
 
 public static void ShowNum(int number,int x,int y,int s,int NumLenth,int Align,int[] ACTPtr,int ACT_SPD){
 	number = Math.abs(number);
 	switch(Align){
 	// :cond_0
 	case 3:  case 4: 
 	return;
 	case 1: 
 	int Bit = info.pharos.ly.C_PUB.getScoreLength(number);
 	y = (y + (Bit * s));
 	int i = 0;
 	while (i < 8) {
 		if (number != 0 || i < NumLenth) {
 			int Num = (number % 0xa);
 			number = (number / 0xa);
 			info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().WriteSprite(ACTPtr[Num],x,(y - (i * s)),ACT_SPD);
 			//[OTHER] end local v1           #Num:I
 			// :cond_2
 			}
 		i = (i + 0x1);
 	}//goto somewhere; //maybe return,continue,break: goto :goto_0
 	break;
 	case 5: 
 	Bit = info.pharos.ly.C_PUB.getScoreLength(number);
 	if (Bit < NumLenth) {
 	Bit = NumLenth;
 	// :cond_3
 	}
 	y = (y + ((s * Bit) / 0x2));
 	i = 0 /* 0 */;
 	while (i < 8) {
 		if (number != 0 || i < NumLenth) {
 		// :cond_4
 			int Num = (number % 0xa);
 			number = (number / 0xa);
 			info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().WriteSprite(ACTPtr[Num],x,(y - (i * s)),ACT_SPD);
 		}
 	//[OTHER] end local v1           #Num:I
 	// :cond_5
 		i = (i + 0x1);
 	}
 	break;//goto somewhere; //maybe return,continue,break: goto :goto_1
 	case 2: 
 	i = 0 /* 0 */;
 	
 	while (i < 8) {
 	if (number != 0 || i < NumLenth) {
 	// :cond_6
 	int Num = (number % 0xa);
 	
 	number = (number / 0xa);
 	info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().WriteSprite(ACTPtr[Num],x,(y - (i * s)),ACT_SPD);
 	//[OTHER] end local v1           #Num:I
 	// :cond_7
 	}
 	i = (i + 0x1);
 	}//	goto somewhere; //maybe return,continue,break: goto :goto_2
 	break;
 	} //end of switch
 	//[OTHER] end local v0           #Bit:I
 	//[OTHER] end local v2           #i:I
 	//[OTHER] end local v0           #Bit:I
 	//[OTHER] end local v2           #i:I
 }
 
 public static void ShowNum_Dec(long number,int x,int y,int s){
 	long Value = Math.abs(number);
 	int i = 0;
 	//[OTHER] end local v2           #i:I
 	//int i = i;
 	//[OTHER] end local v0           #Value:J
 	//long Value = Value;
 	//[OTHER] end local v7           #Value:J
 	//long Value = Value;
 	//Value = 8;
 		while (i < 8) {
 			if (((Value - 0) != 0 || i == 0)) {
 				// :cond_3
 				Value = (Value % 10);
 				int Num = (int) Value;
 
 				Value = (Value / 10);
 				// [OTHER] end local v0 #Num:I
 				info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas()
 						.WriteSprite(
 								info.pharos.ly.C_DEF.TESTNUMTBL[Num], x,
 								(y - (i * s)), 7);
 				// [OTHER] end local v3 #i:I
 				i = (i + 0x1);
 				// i = i;
 				// [OTHER] end local v0 #i:I
 
 			}
 			// goto :goto_0
 			// :cond_2
 		}
 	// :cond_0
 	if ((number - 0) < 0) {
 	//[OTHER] end local p0
 	//com.game.UnicornDash.C_OPhoneApp.cLib = com.game.UnicornDash.C_OPhoneApp.cLib.getGameCanvas();
 //	Value = (i + 0x1);
 //	s = (s * Value);
 //	y = (y - s);
 //	s = 7;
 	info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().WriteSprite(info.pharos.ly.C_DEF.TESTNUMTBL[16],x,y-s*(i+1),7);
 	//[OTHER] end local p3
 	//[OTHER] end local p4
 	// :cond_1
 	}
 	return;
 	
 	
 	
 }
 
 public static void ShowNum_Hex(long number,int x,int y,int s){
 	int i = 0;
 	while (i < 8) {
 	int Value = 15;
 	info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().WriteSprite(info.pharos.ly.C_DEF.TESTNUMTBL[Value],x,(y - (i * s)),7);
 	number = (number >> 4);
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_0
 	}
 	return;
 }
 
 public static void To_Buy(){
 	android.net.Uri uri = android.net.Uri.parse("market://details?id=com.wuzla.game.UnicornDash_Paid_L");
 	;
 	android.content.Intent it = new android.content.Intent("android.intent.action.VIEW",uri);
 	it.addFlags(0x10000000);
 	info.pharos.ly.C_OPhoneApp.cLib.GetActivity().startActivity(it);
 	info.pharos.ly.C_PUB.InitTouch();
 	return;
 }
 
 public static void To_More(){
 	info.pharos.ly.UnicornDash activity = (info.pharos.ly.UnicornDash)info.pharos.ly.C_OPhoneApp.cLib.GetActivity();
 	if (activity != null) {
 	activity.clickMoreGames();
 	// :cond_0
 	}
 	return;
 }
 
 public static int getScoreLength(int num){
 	int bit = 0;
 	num = Math.abs(num);
 	do {
 	bit = (bit + 0x1);
 	num = (num / 0xa);
 	} while (num != 0);
 	return bit;
 }
 
 public static void setGameMode(int mGameMode){
 	info.pharos.ly.C_Global.g_GameMode = mGameMode;
 	return;
 }
 
 public static void setGameState(int mGameState){
 	info.pharos.ly.C_Global.g_GameState = mGameState;
 	return;
 }
 
 public static void setMusicStatus(){
 	info.pharos.ly.C_Save.g_MusicStatus = info.pharos.ly.C_OPhoneApp.cLib.getMediaManager().GetMediaStatus();
 	if (!info.pharos.ly.C_Save.g_MusicStatus) {
 	info.pharos.ly.C_OPhoneApp.cLib.getMediaManager().SetMediaEnable(true);
 	if (info.pharos.ly.C_Global.g_GameMode == 5) {
 	info.pharos.ly.C_Media.PlayMenuMusic();
 	} else {
 	info.pharos.ly.C_Media.PlayGameMusic();
 		}
 	// goto :goto_0
 	} else {
	info.pharos.ly.C_OPhoneApp.cLib.getMediaManager().CH_MediaStop(0);
	info.pharos.ly.C_OPhoneApp.cLib.getMediaManager().SetMediaEnable(false);
 		}
 	// goto :goto_0
 	// :goto_0
 	return;
 }
 
 public static void setSoundStatus(){
 	info.pharos.ly.C_Save.g_SoundStatus = info.pharos.ly.C_OPhoneApp.cLib.getMediaManager().GetSoundStatus();
 	if (!info.pharos.ly.C_Save.g_SoundStatus) {
 	info.pharos.ly.C_OPhoneApp.cLib.getMediaManager().SetSoundEnable(true);
 	info.pharos.ly.C_Media.PlaySound(5);
 	} else {
 	info.pharos.ly.C_OPhoneApp.cLib.getMediaManager().SetSoundEnable(false);
 		}
 	// goto :goto_0
 	// :goto_0
 	return;
 }
 
 public static void setVibratorStatus(){
 	info.pharos.ly.C_Save.g_ShakeStatus = info.pharos.ly.Vibrator.enabled;
 	if (!info.pharos.ly.C_Save.g_ShakeStatus) {
 	info.pharos.ly.Vibrator.enable();
 	} else {
 	info.pharos.ly.Vibrator.disable();
 		}
 	// goto :goto_0
 	// :goto_0
 	info.pharos.ly.C_PUB.setVibratorTime();
 	return;
 }
 
 public static void setVibratorTime(){
 	info.pharos.ly.Vibrator.vibrate(500);
 	return;
 }
 
 public static void showPauseSCR(){
 	int i = 0;
 	while (i < 36) {
 	info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().WriteSprite(2130837682,160,((i * 0x10) - 48),6);
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_0
 	}
 	return;
 }
 
 }

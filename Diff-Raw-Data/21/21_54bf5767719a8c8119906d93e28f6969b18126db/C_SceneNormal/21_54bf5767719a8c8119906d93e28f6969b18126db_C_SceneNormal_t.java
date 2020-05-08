 package info.pharos.ly;
 
 
 public class C_SceneNormal extends Object  {
 
 // static fields
 public static final int HorizontalLine = 0x8c;
 public static final int[] PowerBall_Dash_OffsetTBL;
 public static final double[] ScaleTBL = {1.0D,1.0D,0.9D,0.8D,0.7D,0.6D,0.5D,0.4D,0.3D,0.2D,0.2D,0.1D,0.1D,0.1D,0.2D,0.2D,0.3D,0.3D,0.3D,0.4D,0.5D,0.6D,0.7D,0.8D,0.9D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D,1.0D};
 private static int[] SceneDashTBL = null;
 public static final int TOUCH_BODY_X1 = 0xf;
 public static final int TOUCH_BODY_Y1 = 0xf;
 public static final int TOUCH_FEET_X1 = 0x0;
 public static final int TOUCH_FEET_X2 = 0x0;
 public static final int TOUCH_FEET_Y1 = 0xf;
 public static final int TOUCH_FEET_Y2 = -0xf;
 public static final int TOUCH_HEAD_X1 = 0x2d;
 public static final int TOUCH_HEAD_Y1 = 0xf;
 // instance fields
 private info.pharos.ly.C_EVTDashBTN c_DashBTN;
 private info.pharos.ly.C_EVTEffect[] c_EVTEffect;
 private info.pharos.ly.C_EVTEidolon[] c_EVTEidolon;
 private info.pharos.ly.C_EVTGameBTN[] c_EVTGameBTN;
 private info.pharos.ly.C_EVTLand[] c_EVTLand;
 private info.pharos.ly.C_EVTPlayer c_EVTPlayer;
 private info.pharos.ly.C_EVTPowerBall c_EVTPowerBall;
 private info.pharos.ly.C_EVTPropA[] c_EVTPropA;
 private info.pharos.ly.C_EVTEffect c_Effect;
 private info.pharos.ly.C_EVTEidolon c_Eidolon;
 private info.pharos.ly.C_EVTGameBTN c_GameBTN;
 private info.pharos.ly.C_EVTJumpBTN c_JumpBTN;
 private info.pharos.ly.C_EVTLand c_Land;
 private info.pharos.ly.C_EVTPropA c_PropA;
 private int m_FinalCount;
 private int m_FinalScore;
 private int m_SCRIdx;
 private int m_ScoreCount1;
 private int m_ScoreCount2;
 private int m_ScoreCount3;
 private boolean m_isMsgRun;
 private boolean m_isRun;
 // direct methods
 
 static{
 	int[] v0v132 = new int[21];
 	v0v132[1] = -4;
 	v0v132[2] = 4;
 	v0v132[3] = -6;
 	v0v132[4] = 6;
 	v0v132[5] = -2;
 	v0v132[6] = 2;
 	v0v132[7] = -10;
 	v0v132[8] = 10;
 	v0v132[9] = -4;
 	v0v132[10] = 4;
 	v0v132[11] = -2;
 	v0v132[12] = 2;
 	v0v132[13] = -4;
 	v0v132[14] = 4;
 	v0v132[15] = -2;
 	v0v132[16] = 2;
 	info.pharos.ly.C_SceneNormal.SceneDashTBL = v0v132;
 	;
 	int[] v0v112 = new int[13];
 	v0v112[1] = 1;
 	v0v112[2] = 2;
 	v0v112[3] = 3;
 	v0v112[4] = 4;
 	v0v112[5] = 5;
 	v0v112[6] = 6;
 	v0v112[7] = 6;
 	v0v112[8] = 5;
 	v0v112[9] = 4;
 	v0v112[10] = 3;
 	v0v112[11] = 2;
 	PowerBall_Dash_OffsetTBL = v0v112;
 	// op;
 	;
 }
 
 public C_SceneNormal(){
 	super();
 	this.c_EVTPlayer = null /* 0 */;
 	this.c_Land = null /* 0 */;
 	this.c_GameBTN = null /* 0 */;
 	this.c_PropA = null /* 0 */;
 	this.c_DashBTN = null /* 0 */;
 	this.c_JumpBTN = null /* 0 */;
 	this.c_Eidolon = null /* 0 */;
 	this.c_Effect = null /* 0 */;
 	this.c_EVTPowerBall = null /* 0 */;
 	this.c_EVTLand = null /* 0 */;
 	this.c_EVTGameBTN = null /* 0 */;
 	this.c_EVTPropA = null /* 0 */;
 	this.c_EVTEidolon = null /* 0 */;
 	this.c_EVTEffect = null /* 0 */;
 }
 
 private void AddScoreMain(int AddScore){
 	info.pharos.ly.C_Global.g_RunLength = (info.pharos.ly.C_Global.g_RunLength + (long)((AddScore << 0x10) * 0x5));
 	return;
 }
 
 private void ClearACT(){
 	info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().ClearACT();
 	return;
 }
 
 private void ClearGameBTN(int BTNType){
 	if (this.c_EVTGameBTN != null) {
 	int i = 0;
 	while (i < 5) {
 	if (this.c_EVTGameBTN[i].m_BTNType == BTNType) {
 	this.c_EVTGameBTN[i].EVTCLR();
 	// :cond_2
 	}
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_1
 	}
 	//[OTHER] end local v0           #i:I
 	// :cond_0
 	}
 	return;
 	
 }
 
 private void ExecEVENT(){
 	this.ExecEVT();
 	this.ExecRUN();
 	this.ExecTouch();
 	return;
 }
 
 private void ExecEVT(){
 	;
 	switch(info.pharos.ly.C_Global.g_GameState){
 	case 3: case 7: 
 	if (this.c_EVTPlayer != null) {
 	this.c_EVTPlayer.ExecEVT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
 	// :cond_1
 	}
 	if (this.c_EVTLand != null) {
 	int i = 0 /* 0 */;
 	
 	while (i < 5) {
 	this.c_EVTLand[i].ExecEVT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
 	i = (i + 0x1);
 	// goto :goto_2
 	// :cond_6
 	}
 	//[OTHER] end local v0           #i:I
 	// :cond_2
 	}
 	if (this.c_EVTPropA != null) {
 		int i = 0 /* 0 */;
 	
 	while (i < 5) {
 	this.c_EVTPropA[i].ExecEVT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
 	i = (i + 0x1);
 	// goto :goto_3
 	// :cond_7
 	}
 	//[OTHER] end local v0           #i:I
 	// :cond_3
 	}
 	if (this.c_EVTEidolon != null) {
 		int i = 0 /* 0 */;
 	
 	while (i < 2) {
 	this.c_EVTEidolon[i].ExecEVT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
  i = (i + 0x1);
 	// goto :goto_4
 	// :cond_8
 	}
 	//[OTHER] end local v0           #i:I
 	// :cond_4
 	}
 	if (this.c_EVTEffect != null) {
 		int i = 0 /* 0 */;
 	
 	while (i < 10) {
 	this.c_EVTEffect[i].ExecEVT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
 	i = (i + 0x1);
 	// goto :goto_5
 	// :cond_9
 	}
 	//[OTHER] end local v0           #i:I
 	// :cond_5
 	}
 	this.c_DashBTN.ExecEVT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
 	this.c_JumpBTN.ExecEVT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
 	this.c_EVTPowerBall.ExecEVT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
 	break;
 	} //end of switch
 	if (this.c_EVTGameBTN != null) {
 	int i = 0;
 	while (i < 5) {
 	this.c_EVTGameBTN[i].ExecEVT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
 	i = (i + 0x1);
 	// goto/16 :goto_1
 	// :cond_a
 	}
 	//[OTHER] end local v0           #i:I
 	// :cond_0
 	}
 	return;
 	
 	// op;
 	
 }
 
 private void ExecRUN(){
 		if (info.pharos.ly.C_Global.g_GameState == 7) {
 			if (this.c_EVTPlayer != null) {
 				this.c_EVTPlayer.ExecRUN(info.pharos.ly.C_OPhoneApp.cLib
 						.getGameCanvas());
 				// :cond_0
 			}
 			if (this.c_EVTLand != null) {
 				int i = 0;
 				while (i < 5) {
 					this.c_EVTLand[i].ExecRUN(info.pharos.ly.C_OPhoneApp.cLib
 							.getGameCanvas());
 					i = (i + 0x1);
 					// goto :goto_0
 					// :cond_7
 				}
 				// [OTHER] end local v0 #i:I
 				// :cond_1
 			}
 			if (this.c_EVTPropA != null) {
 				int i = 0 /* 0 */;
 
 				while (i < 5) {
 					this.c_EVTPropA[i].ExecRUN(info.pharos.ly.C_OPhoneApp.cLib
 							.getGameCanvas());
 					i = (i + 0x1);
 					// goto :goto_1
 					// :cond_8
 				}
 				// [OTHER] end local v0 #i:I
 				// :cond_2
 			}
 			if (this.c_EVTEidolon != null) {
 				int i = 0 /* 0 */;
 
 				while (i < 2) {
 					this.c_EVTEidolon[i]
 							.ExecRUN(info.pharos.ly.C_OPhoneApp.cLib
 									.getGameCanvas());
 					i = (i + 0x1);
 					// goto :goto_2
 					// :cond_9
 				}
 				// [OTHER] end local v0 #i:I
 				// :cond_3
 			}
 			if (this.c_EVTEffect != null) {
 				int i = 0 /* 0 */;
 
 				while (i < 10) {
 					this.c_EVTEffect[i].ExecRUN(info.pharos.ly.C_OPhoneApp.cLib
 							.getGameCanvas());
 					i = (i + 0x1);
 					// goto :goto_3
 					// :cond_a
 				}
 				// [OTHER] end local v0 #i:I
 				// :cond_4
 			}
 			this.c_DashBTN.ExecRUN(info.pharos.ly.C_OPhoneApp.cLib
 					.getGameCanvas());
 			this.c_JumpBTN.ExecRUN(info.pharos.ly.C_OPhoneApp.cLib
 					.getGameCanvas());
 			this.c_EVTPowerBall.ExecRUN(info.pharos.ly.C_OPhoneApp.cLib
 					.getGameCanvas());
 			// :cond_5
 		}
 		if (this.c_EVTGameBTN != null) {
 			int i = 0 /* 0 */;
 
 			while (i < 5) {
 				this.c_EVTGameBTN[i].ExecRUN(info.pharos.ly.C_OPhoneApp.cLib
 						.getGameCanvas());
 				i = (i + 0x1);
 				// goto :goto_4
 				// :cond_b
 			}
 			// [OTHER] end local v0 #i:I
 			// :cond_6
 		}
 		return;
 
 }
 
 	private void ExecTouch() {
 		if (this.c_EVTPlayer.EVT.Ctrl == 4) {
 			// :cond_0
 			return;
 		}
 		int Var1 = (this.getLandSpeed() >> 0x10);
 		int Var2 = (this.c_EVTPlayer.EVT.XInc >> 0x10);
 		if (Var2 > 0) {
 			Var2 = 0 /* 0 */;
 			// :cond_2
 		}
 		int MaxAdjust = ((Var1 - Var2) * 0x2);
 		boolean Front_Test = false;
 		boolean Back_Test = false;
 		if (this.c_EVTLand != null && this.c_EVTPlayer != null) {
 			this.c_EVTPlayer.m_isGround = false /* 0 */;
 			int Feet_X1 = ((((this.c_EVTPlayer.EVT.XVal >> 0x10) + 0x0) + info.pharos.ly.C_Global.g_SceneFloat_X) + 0x0);
 			int Feet_Y1 = (((this.c_EVTPlayer.EVT.YVal >> 0x10) + 0xf) + info.pharos.ly.C_Global.g_SceneFloat_Y);
 			int Feet_X2 = ((((this.c_EVTPlayer.EVT.XVal >> 0x10) + 0x0) + info.pharos.ly.C_Global.g_SceneFloat_X) + 0x0);
 			int Feet_Y2 = (((this.c_EVTPlayer.EVT.YVal >> 0x10) + -0xf) + info.pharos.ly.C_Global.g_SceneFloat_Y);
 			int Body_X1 = (((this.c_EVTPlayer.EVT.XVal >> 0x10) + 0xf) + info.pharos.ly.C_Global.g_SceneFloat_X);
 			int Body_Y1 = (((this.c_EVTPlayer.EVT.YVal >> 0x10) + 0xf) + info.pharos.ly.C_Global.g_SceneFloat_Y);
 			int Head_X1 = (info.pharos.ly.C_Global.g_SceneFloat_X + ((this.c_EVTPlayer.EVT.XVal >> 0x10) + 0x2d));
 			int Head_Y1 = (info.pharos.ly.C_Global.g_SceneFloat_Y + ((this.c_EVTPlayer.EVT.YVal >> 0x10) + 0xf));
 			int Front_X = (this.c_EVTPlayer.EVT.XVal >> 0x10);
 			int Back_X = (this.c_EVTPlayer.EVT.XVal >> 0x10);
 			int i = 0;
 			while (i < 5) {
 				// :cond_7
 				if (this.c_EVTLand[i].EVT.Valid) {
 					if (this.c_EVTPlayer.EVT.Ctrl == 0
 							|| this.c_EVTPlayer.EVT.Ctrl == 3) {
 						// :cond_8
 						switch (this.c_EVTLand[i].EVT.Ctrl) {
 						case 5:
 						case 10:
 							if (MaxAdjust > 32) {
 								MaxAdjust = 32;
 								break;
 							} // end of switch
 								// :cond_9
 						}
 					}
 
 					int Idx = 0;
 					int Ctrl;
 					do {
 						// [OTHER] end local v6 #Head_X1:I
 						int ACTName = this.c_EVTLand[i].EVT.ACTPtr[this.c_EVTLand[i].EVT.Ctrl][Idx];
 						int XVal = (this.c_EVTLand[i].EVT.XVal >> 0x10);
 						int YVal = (this.c_EVTLand[i].EVT.YVal >> 0x10);
 						Ctrl = this.c_EVTLand[i].EVT.Ctrl;
 
 						// Head_Y1 = ((this.c_EVTPlayer.EVT.YVal >> 0x10) +
 						// 0xf);
 						Head_X1 = ((this.c_EVTPlayer.EVT.XVal >> 16) + 45);
 						Head_Y1 = ((this.c_EVTPlayer.EVT.YVal >> 16) + 15);
 
 						if (info.pharos.ly.C_OPhoneApp.cLib
 								.getGameCanvas().CHKACTTouch(Head_X1, Head_Y1,
 										1, 1, 1, 1, ACTName, XVal, YVal)) {
 							int Area = 0 /* 0 */;
 							while (info.pharos.ly.C_PUB.GetActPixel(
 									ACTName, XVal, YVal, Head_X1, Head_Y1) != 0) {
 								Head_X1 = Head_X1 - 1;
 								Area = (Area + 0x1);
 								if (Area > (MaxAdjust + 0x2)) {
 									// Head_X1=4;
 									// Head_Y1=0;
 									this.c_EVTPlayer.setPlayerCtrl(4, 0);
 									return;
 								}
 							}
 							info.pharos.ly.C_Global.g_SceneFloat_X = (info.pharos.ly.C_Global.g_SceneFloat_X + Area);
 							this.c_EVTPlayer.EVT.XVal = ((Head_X1 - 45) << 0x10);
 						}
 						Feet_X1 = (((this.c_EVTPlayer.EVT.XVal >> 0x10) + 0x0) + 0x0);
 						Feet_Y1 = ((this.c_EVTPlayer.EVT.YVal >> 0x10) + 0xf);
 
 						if (info.pharos.ly.C_OPhoneApp.cLib
 								.getGameCanvas().CHKACTTouch(Feet_X1, Feet_Y1,
 										1, 1, 1, 1, ACTName, XVal, YVal)) {
 							if (this.c_EVTPlayer.EVT.Ctrl == 0
 									&& info.pharos.ly.C_PUB.GetActPixel(
 											ACTName, XVal, YVal, Feet_X1,
 											Feet_Y1) == 0
 									&& info.pharos.ly.C_PUB.GetActPixel(
 											ACTName, XVal, YVal,
 											(Feet_X1 - MaxAdjust), Feet_Y1) != 0) {
 								Front_X = (Front_X - (MaxAdjust / 0x2));
 								Feet_X1 = (Feet_X1 - (MaxAdjust / 0x2));
 								Front_Test = true;
 								// :cond_d
 							}
 							int Area = 0 /* 0 */;
 							// :cond_e
 							while (info.pharos.ly.C_PUB.GetActPixel(
 									ACTName, XVal, YVal, Feet_X1, Feet_Y1) != 0) {
 								// [OTHER] end local v38 #Area:I
 								// :cond_1b
 
 								// :cond_14
 								this.c_EVTPlayer.m_isGround = true;
 								Feet_X1 = (Feet_X1 + 0x1);
 								Area = (Area + 0x1);
 								if (Area <= (MaxAdjust + 0x2)) {
 									if (Front_X < Feet_X1) {
 										Front_X = Feet_X1;
 									}
 								} else {
 									this.c_EVTPlayer.setPlayerCtrl(4, 0);
 									return;
 								}
 							}
 						}
 						Feet_X2 = (((this.c_EVTPlayer.EVT.XVal >> 0x10) + 0x0) + 0x0);
 
 						Feet_Y2 = ((this.c_EVTPlayer.EVT.YVal >> 0x10) + -0xf);
 						if (info.pharos.ly.C_OPhoneApp.cLib
 								.getGameCanvas().CHKACTTouch(Feet_X2, Feet_Y2,
 										1, 1, 1, 1, ACTName, XVal, YVal)) {
 							if (this.c_EVTPlayer.EVT.Ctrl == 0
 									&& info.pharos.ly.C_PUB.GetActPixel(
 											ACTName, XVal, YVal, Feet_X2,
 											Feet_Y2) == 0
 									&& info.pharos.ly.C_PUB.GetActPixel(
 											ACTName, XVal, YVal,
 											(Feet_X2 - MaxAdjust), Feet_Y2) != 0) {
 								Back_X = (Back_X - (MaxAdjust / 0x2));
 								Feet_X2 = (Feet_X2 - (MaxAdjust / 0x2));
 								Back_Test = true;
 								// :cond_10
 							}
 							int Area = 0 /* 0 */;
 							// :cond_11
 							while (info.pharos.ly.C_PUB.GetActPixel(
 									ACTName, XVal, YVal, Feet_X2, Feet_Y2) != 0) {
 								// [OTHER] end local v38 #Area:I
 								// :cond_f
 								// :cond_16
 								this.c_EVTPlayer.m_isGround = true;
 								Feet_X2 = (Feet_X2 + 0x1);
 								Area = (Area + 0x1);
 								if (Area > (MaxAdjust + 0x2)) {
 									//Head_Y1 = 0 /* 0 */;
 									this.c_EVTPlayer.setPlayerCtrl(4, 0);
 									return;
 									// :cond_b
 								} else {
 									if (Back_X < Feet_X2) {
 										Back_X = Feet_X2;
 									}
 								}
 
 							}
 
 						}
 						Body_X1 = (Front_X + 0xf);
 
 						Body_Y1 = ((this.c_EVTPlayer.EVT.YVal >> 0x10) + 0xf);
 						if (info.pharos.ly.C_OPhoneApp.cLib
 								.getGameCanvas().CHKACTTouch(Body_X1, Body_Y1,
 										1, 1, 1, 1, ACTName, XVal, YVal)
 								&& info.pharos.ly.C_PUB.GetActPixel(
 										ACTName, XVal, YVal, Body_X1, Body_Y1) != 0
 								&& !Front_Test) {
 							// Head_Y1 = 0 /* 0 */;
 							this.c_EVTPlayer.setPlayerCtrl(4, 0);
 							return;
 						} else {
 							if ((this.c_EVTPlayer.EVT.Ctrl == 1 || this.c_EVTPlayer.EVT.Ctrl == 2)
 									&& this.c_EVTPlayer.m_isGround
 									&& this.c_EVTPlayer.EVT.XInc <= 0) {
 								// :cond_19
 								this.c_EVTPlayer.setPlayerCtrl(0, 0);
 								// :cond_1a
 							}
 							Idx = (Idx + 0x1);
 						}
 
 					} while (this.GetLandAct(i, Ctrl, Idx) != -1);
 
 				}
 				i++;
 
 			}
 
 			if (this.c_EVTPlayer.m_isGround) {
 				this.c_EVTPlayer.CHKGameOperateClr();
 				// [OTHER] end local v6 #Head_X1:I
 				this.c_EVTPlayer.EVT.XVal = (((Front_X + Back_X) / 0x2) << 0x10);
 				// :cond_3
 			}
 			if (this.c_EVTPlayer.EVT.Ctrl == 0) {
 				this.c_EVTPlayer.m_BodyRadian = Math.atan2(
 						(double) (Back_X - Front_X),
 						(double) (Feet_Y1 - Feet_Y2));
 				this.c_EVTPlayer.EVT.Rotate = (float) info.pharos.ly.C_PUB
 						.RadianToAngle(this.c_EVTPlayer.m_BodyRadian);
 				// [OTHER] end local v7 #Head_Y1:I
 				// [OTHER] end local v16 #Feet_X1:I
 				// [OTHER] end local v17 #Feet_Y1:I
 				// [OTHER] end local v19 #Feet_X2:I
 				// [OTHER] end local v20 #Feet_Y2:I
 				// [OTHER] end local v22 #Body_X1:I
 				// [OTHER] end local v23 #Body_Y1:I
 				// [OTHER] end local v40 #Back_X:I
 				// [OTHER] end local v43 #Front_X:I
 				// [OTHER] end local v48 #i:I
 				// goto/16 :goto_2
 			} else {
 				this.c_EVTPlayer.EVT.Rotate = 0 /* 0 */;
 			}
 			// :cond_4
 		}
 
 		// :goto_2
 		if (this.c_EVTPlayer != null) {
 			int SACTName = this.c_EVTPlayer.EVT.ACTIdx;
 			int SXVal = (this.c_EVTPlayer.EVT.XVal >> 0x10);
 			int SYVal = (this.c_EVTPlayer.EVT.YVal >> 0x10);
 			if (this.c_EVTPropA != null) {
 				int i = 0;
 				while (i < 5) {
 					if (this.c_EVTPropA[i].EVT.Valid
 							&& this.c_EVTPropA[i].EVT.Ctrl == 0) {
 						int DACTName = this.c_EVTPropA[i].EVT.ACTIdx;
 						int DXVal = (this.c_EVTPropA[i].EVT.XVal >> 0x10);
 						int DYVal = (this.c_EVTPropA[i].EVT.YVal >> 0x10);
 						if (info.pharos.ly.C_OPhoneApp.cLib
 								.getGameCanvas().CHKACTTouch(SACTName, SXVal,
 										SYVal, DACTName, DXVal, DYVal)) {
 							if (this.c_EVTPlayer.EVT.Ctrl == 3) {
 								this.c_EVTPropA[i].SetEVTCtrl(1, 0);
 								info.pharos.ly.C_Media.PlaySound(1);
 								info.pharos.ly.C_PUB.setVibratorTime();
 								info.pharos.ly.C_OPhoneApp.cLib
 										.getMessageMgr()
 										.SendMessage(
 												40,
 												42,
 												0,
 												0,
 												DXVal,
 												(DYVal - (this.getLandSpeed() >> 0x10)));
 								info.pharos.ly.C_OPhoneApp.cLib
 										.getMessageMgr().SendMessage(40, 45, 0);
 
 							} else {
 								if (this.c_EVTPowerBall.EVT.Valid
 										&& this.c_EVTPowerBall.EVT.Ctrl == 1) {
 									this.c_EVTPowerBall.SetEVTCtrl(2, 0);
 									this.c_EVTPropA[i].SetEVTCtrl(1, 0);
 									info.pharos.ly.C_Media.PlaySound(1);
 									info.pharos.ly.C_PUB
 											.setVibratorTime();
 									info.pharos.ly.C_OPhoneApp.cLib
 											.getMessageMgr()
 											.SendMessage(
 													40,
 													42,
 													0,
 													0,
 													DXVal,
 													(DYVal - (this
 															.getLandSpeed() >> 0x10)));
 									info.pharos.ly.C_OPhoneApp.cLib
 											.getMessageMgr().SendMessage(40,
 													45, 0);
 								} else {
 									this.c_EVTPlayer.setPlayerCtrl(4, 0);
 									return;
 								}
 
 							}
 						}
 					}
 					i++;
 				}
 			}
 
 			if (this.c_EVTEidolon != null) {
 				int i = 0;
 				while (i < 2) {
 					if (this.c_EVTEidolon[i].EVT.Valid
 							&& this.c_EVTEidolon[i].EVT.Ctrl == 0) {
 						int DACTName = this.c_EVTEidolon[i].EVT.ACTIdx;
 
 						int DXVal = (this.c_EVTEidolon[i].EVT.XVal >> 0x10);
 
 						int DYVal = (this.c_EVTEidolon[i].EVT.YVal >> 0x10);
 						if (info.pharos.ly.C_OPhoneApp.cLib
 								.getGameCanvas().CHKACTTouch(SACTName, SXVal,
 										SYVal, DACTName, DXVal, DYVal)) {
 							this.c_EVTEidolon[i].SetEVTCtrl(1, 0);
 							info.pharos.ly.C_Media.PlaySound(6);
 							info.pharos.ly.C_OPhoneApp.cLib
 									.getMessageMgr()
 									.SendMessage(
 											40,
 											44,
 											0,
 											0,
 											DXVal,
 											(DYVal - (this.getLandSpeed() >> 0x10)));
 						}
 
 					}
 					i++;
 				}
 			}
 
 			if (this.c_EVTPowerBall.EVT.Valid
 					&& this.c_EVTPowerBall.EVT.Ctrl == 0) {
 				int DACTName = this.c_EVTPowerBall.EVT.ACTIdx;
 				int DXVal = (this.c_EVTPowerBall.EVT.XVal >> 0x10);
 				int DYVal = (this.c_EVTPowerBall.EVT.YVal >> 0x10);
 				if (info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas()
 						.CHKACTTouch(SACTName, SXVal, SYVal, DACTName, DXVal,
 								DYVal)) {
 					this.c_EVTPowerBall.SetEVTCtrl(1, 0);
 					info.pharos.ly.C_Media.PlaySound(6);
 					return;
 				}
 
 			}
 
 		}
 
 	}
 
 private int GetLandAct(int EVTIdx,int EVTCtrl,int ACTIdx){
 	;
 	return this.c_EVTLand[EVTIdx].EVT.ACTPtr[EVTCtrl][ACTIdx];
 	//return this.c_EVTLand[EVTIdx].EVT.ACTPtr[EVTCtrl][ACTIdx];
 }
 
 private void InitAgain(){
 	this.m_SCRIdx = (3 - info.pharos.ly.C_Global.g_GameLife);
 	info.pharos.ly.C_Global.g_LandMakeArea = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_LandSpace = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_RunLength = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_CurDistance = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_LandMoveSpeed = 0xa0000;
 	info.pharos.ly.C_Global.g_DashSpeedArea = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_EidolonCount = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_PropACount = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_SceneFloat_X = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_MakeCloudDly = 0 /* 0 */;
 	this.c_EVTPlayer.CreatePlayer();
 	info.pharos.ly.C_Global.g_LandIdx = 0 /* 0 */;
 	this.c_Land.CreateLand(100,60,false);
 	info.pharos.ly.C_Global.g_LandIdx = 0 /* 0 */;
 	this.c_Land.CreateLand(100,1392,false);
 	this.c_DashBTN.CreateDashBTN(this.c_DashBTN,40,(info.pharos.ly.C_Global.g_ScreenScale_3_1 + 0x1a4));
 	this.c_JumpBTN.CreateJumpBTN(this.c_JumpBTN,40,(60 - info.pharos.ly.C_Global.g_ScreenScale_3_1));
 	this.c_GameBTN.CreateGameBTN(6,300,(50 - info.pharos.ly.C_Global.g_ScreenScale_3_1));
 	return;
 }
 
 private void Initialize(){
 	this.m_isRun = true;
 	this.m_isMsgRun = true;
 	info.pharos.ly.C_Global.g_LandIdx = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_LandTest = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_GameLife = 3;
 	info.pharos.ly.C_Global.g_Dash1Score = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_Dash2Score = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_Dash3Score = 0 /* 0 */;
 	;
 	this.c_EVTPlayer = new info.pharos.ly.C_EVTPlayer();
 	;
 	this.c_Land = new info.pharos.ly.C_EVTLand();
 	;
 	this.c_GameBTN = new info.pharos.ly.C_EVTGameBTN();
 	;
 	this.c_PropA = new info.pharos.ly.C_EVTPropA();
 	;
 	this.c_DashBTN = new info.pharos.ly.C_EVTDashBTN();
 	;
 	this.c_JumpBTN = new info.pharos.ly.C_EVTJumpBTN();
 	;
 	this.c_Eidolon = new info.pharos.ly.C_EVTEidolon();
 	;
 	this.c_Effect = new info.pharos.ly.C_EVTEffect();
 	;
 	this.c_EVTPowerBall = new info.pharos.ly.C_EVTPowerBall();
 	;
 	this.c_EVTLand = new info.pharos.ly.C_EVTLand[5];
 	int i = 0;
 	while (i < 5) {
 	;
 	this.c_EVTLand[i] = new info.pharos.ly.C_EVTLand();
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_0
 	}
 	;
 	this.c_EVTGameBTN = new info.pharos.ly.C_EVTGameBTN[5];
 	i = 0 /* 0 */;
 	while (i < 5) {
 	;
 	this.c_EVTGameBTN[i] = new info.pharos.ly.C_EVTGameBTN();
 	i = (i + 0x1);
 	// goto :goto_1
 	// :cond_1
 	}
 	;
 	this.c_EVTPropA = new info.pharos.ly.C_EVTPropA[5];
 	i = 0 /* 0 */;
 	while (i < 5) {
 	;
 	this.c_EVTPropA[i] = new info.pharos.ly.C_EVTPropA();
 	i = (i + 0x1);
 	// goto :goto_2
 	// :cond_2
 	}
 	;
 	this.c_EVTEidolon = new info.pharos.ly.C_EVTEidolon[2];
 	i = 0 /* 0 */;
 	while (i < 2) {
 	;
 	this.c_EVTEidolon[i] = new info.pharos.ly.C_EVTEidolon();
 	i = (i + 0x1);
 	// goto :goto_3
 	// :cond_3
 	}
 	;
 	this.c_EVTEffect = new info.pharos.ly.C_EVTEffect[10];
 	i = 0 /* 0 */;
 	while (i < 10) {
 	;
 	this.c_EVTEffect[i] = new info.pharos.ly.C_EVTEffect();
 	i = (i + 0x1);
 	// goto :goto_4
 	// :cond_4
 	}
 	this.c_Land.InitNPCLandOBJ(this.c_EVTLand);
 	this.c_EVTPlayer.InitPlaneOBJ(this.c_EVTPlayer);
 	this.c_PropA.InitPropAOBJ(this.c_EVTPropA);
 	this.c_GameBTN.InitGameBTNOBJ(this.c_EVTGameBTN);
 	this.c_Eidolon.InitEidolonOBJ(this.c_EVTEidolon);
 	this.c_Effect.InitEffectOBJ(this.c_EVTEffect);
 	info.pharos.ly.C_PUB.InitTouch();
 	info.pharos.ly.C_PUB.setGameState(1);
 	return;
 }
 
 private void LandMain(){
 		if (!this.c_EVTPlayer.EVT.Valid) {
 			// :cond_0
 		} else {
 			if (this.c_EVTPlayer.EVT.Ctrl != 4) {
 				if (info.pharos.ly.C_Global.g_LandMoveSpeed < 0x140000) {
 					info.pharos.ly.C_Global.g_LandMoveSpeed = (info.pharos.ly.C_Global.g_LandMoveSpeed + 0x100);
 					// :cond_2
 				}
 				if (this.c_EVTPlayer.EVT.Ctrl == 3) {
 					info.pharos.ly.C_Global.g_DashSpeedArea = (info.pharos.ly.C_Global.g_DashSpeedArea + 0x10000);
 					if (info.pharos.ly.C_Global.g_DashSpeedArea > 0x80000) {
 						info.pharos.ly.C_Global.g_DashSpeedArea = 0x80000;
 						// :cond_3
 					}
 					// :cond_9
 				}else{
 					
					info.pharos.ly.C_Global.g_DashSpeedArea = (info.pharos.ly.C_Global.g_DashSpeedArea - 0x10000);
 					if (info.pharos.ly.C_Global.g_DashSpeedArea < 0) {
 						info.pharos.ly.C_Global.g_DashSpeedArea = 0 /* 0 */;
 					}
 				}
 				// goto :goto_1
 
 				if (this.c_EVTLand != null) {
 					int i = 0;
 					while (i < 5) {
 						if (this.c_EVTLand[i].EVT.Valid) {
 							this.c_EVTLand[i].EVT.YVal = (this.c_EVTLand[i].EVT.YVal - this
 									.getLandSpeed());
 							int YVal = (this.c_EVTLand[i].EVT.YVal >> 0x10);
 							// this.c_EVTLand[i].m_LandLen =
 							// -this.c_EVTLand[i].m_LandLen;
 							if (YVal < -this.c_EVTLand[i].m_LandLen) {
 								this.c_EVTLand[i].EVTCLR();
 								// [OTHER] end local v0 #YVal:I
 								// :cond_b
 							}
 						}
 						i = (i + 0x1);
 						// goto/16 :goto_2
 						// :cond_a
 					}
 					// [OTHER] end local v1 #i:I
 					// :cond_4
 				}
 				if (this.c_EVTPowerBall.EVT.Valid) {
 					this.c_EVTPowerBall.EVT.YVal = (this.c_EVTPowerBall.EVT.YVal - this
 							.getLandSpeed());
 					// :cond_5
 				}
 				if (this.c_EVTPropA != null) {
 					int i = 0 /* 0 */;
 
 					while (i < 5) {
 						if (this.c_EVTPropA[i].EVT.Valid) {
 							if (this.c_EVTPropA[i].EVT.Ctrl == 0) {
 								this.c_EVTPropA[i].EVT.YVal = (this.c_EVTPropA[i].EVT.YVal - this
 										.getLandSpeed());
 							} else {
 								this.c_EVTPropA[i].EVT.YVal = (this.c_EVTPropA[i].EVT.YVal - ((this
 										.getLandSpeed() * 0x2) / 0x3));
 							}
 							// goto :goto_6
 							// :cond_d
 						}
 						// :goto_6
 						i = (i + 0x1);
 						// goto/16 :goto_3
 						// :cond_c
 					}
 					// [OTHER] end local v1 #i:I
 					// :cond_6
 				}
 				if (this.c_EVTEffect != null) {
 					int i = 0 /* 0 */;
 
 					while (i < 10) {
 						if (this.c_EVTEffect[i].EVT.Ctrl == 3) {
 							this.c_EVTEffect[i].EVT.YVal = (int) ((double) this.c_EVTEffect[i].EVT.YVal - ((double) this
 									.getLandSpeed() * 1.5D));
 							// :cond_10
 						}
 						i = (i + 0x1);
 						// goto/16 :goto_4
 						// :cond_f
 					}
 					// [OTHER] end local v1 #i:I
 					// :cond_7
 				}
 				if (this.c_EVTEidolon != null) {
 					int i = 0 /* 0 */;
 
 					while (i < 2) {
 						if (this.c_EVTEidolon[i].EVT.Valid) {
 							if (this.c_EVTEidolon[i].EVT.Ctrl == 0) {
 								this.c_EVTEidolon[i].EVT.YVal = (this.c_EVTEidolon[i].EVT.YVal - this
 										.getLandSpeed());
 							} else {
 								this.c_EVTEidolon[i].EVT.YVal = (this.c_EVTEidolon[i].EVT.YVal - ((this
 										.getLandSpeed() * 0x2) / 0x3));
 							}
 							// goto :goto_7
 							// :cond_12
 						}
 						// :goto_7
 						i = (i + 0x1);
 						// goto/16 :goto_5
 						// :cond_11
 					}
 					// [OTHER] end local v1 #i:I
 					// :cond_8
 				}
 				info.pharos.ly.C_Global.g_LandSpace = (info.pharos.ly.C_Global.g_LandSpace - this
 						.getLandSpeed());
 				if (info.pharos.ly.C_Global.g_LandSpace <= 0) {
 					this.c_Land.CreateLand(30, 480, true);
 				}
 			}
 		}
 		// goto :goto_0
 		// :goto_0
 		return;
 }
 
 private void MakeHorseEff(){
 	int XVal = (this.c_EVTPlayer.EVT.XVal >> 0x10);
 	int YVal = (this.c_EVTPlayer.EVT.YVal >> 0x10);
 	int Speed = (this.getLandSpeed() >> 0x10);
 	if (this.c_EVTPlayer.EVT.CurCNT == 0 && this.c_EVTPlayer.EVT.Ctrl == 0) {
 	if (this.c_EVTPlayer.EVT.CurFRM == 0) {
 	this.c_Effect.CreateHorse_Eff((XVal + 0x5),(YVal + 0x20),Speed);
 	// :cond_0
 	}
 	if (this.c_EVTPlayer.EVT.CurFRM == 6) {
 	this.c_Effect.CreateHorse_Eff((XVal + 0x5),(YVal - 32),Speed);
 	// :cond_1
 	}
 	}
 	if ((this.c_EVTPlayer.EVT.Ctrl == 1 || this.c_EVTPlayer.EVT.Ctrl == 2) && (info.pharos.ly.C_OPhoneApp.cLib.getVBLCount() % 0x5) == 0) {
 	// :cond_2
 	this.c_Effect.CreateHorse_Eff(((this.c_EVTPlayer.m_FeetOffset_B + XVal) + 0x14),(YVal - 20),Speed);
 	// :cond_3
 	}
 	return;
 }
 
 	private void MsgLoop() {
 		if (!this.m_isMsgRun) {
 		} else {
 			int MSGCount = info.pharos.ly.C_OPhoneApp.cLib
 					.getMessageMgr().GetMessageQueueLength();
 			int i = 0;
 			while (i < MSGCount) {
 				oms.GameEngine.C_MSG Msg = info.pharos.ly.C_OPhoneApp.cLib
 						.getMessageMgr().GetMessageQueue(i);
 				int hWnd = Msg.GetMsgHWnd();
 				int message = Msg.GetMsgMessage();
 				int param = Msg.GetwParam();
 				int cursorX = Msg.GetCursorX();
 				int cursorY = Msg.GetCursorY();
 				switch (hWnd) {
 				case 10:
 					switch (message) {
 					case 3:
 						info.pharos.ly.C_PUB.setSoundStatus();
 						break;
 					case 6:
 						//暂停
 						if (info.pharos.ly.C_Global.g_GameState == 7) {
 //							com.game.UnicornDash.C_PUB.ShowAdView(true);
 							info.pharos.ly.C_Media.StopSound(3);
 							this.ClearGameBTN(6);
 							this.c_GameBTN.CreateGameBTN(7, 190, 240);
 							this.c_GameBTN.CreateGameBTN(8, 130, 240);
 							this.c_GameBTN.CreateGameBTN(3, 70, 170);
 							this.c_GameBTN.CreateGameBTN(4, 70, 240);
 							this.c_GameBTN.CreateGameBTN(5, 70, 310);
 							info.pharos.ly.C_PUB.setGameState(11);
 						}
 						break;
 					case 7:
 						if (this.c_EVTPlayer.EVT.Ctrl == 0) {
 							info.pharos.ly.C_Media.PlaySound(3);
 							// :cond_3
 						}
 //						com.game.UnicornDash.C_PUB.ShowAdView(false);
 						this.ClearGameBTN(7);
 						this.ClearGameBTN(8);
 						this.ClearGameBTN(3);
 						this.ClearGameBTN(4);
 						this.ClearGameBTN(5);
 						this.c_GameBTN.CreateGameBTN(6, 300, 50);
 						info.pharos.ly.C_PUB.setGameState(7);
 						break;
 					case 8:
 						this.m_isRun = false /* 0 */;
 						info.pharos.ly.C_PUB.setGameState(10);
 						break;
 					case 4:
 						info.pharos.ly.C_PUB.setMusicStatus();
 						break;
 					case 5:
 						info.pharos.ly.C_PUB.setVibratorStatus();
 						break;
 					} // end of switch
 					break;
 				case 40:
 					switch (message) {
 					case 46:
 					case 47:
 						break;
 					case 41:
 						this.c_PropA.CreatePropA(param, cursorX, cursorY);
 						break;
 					case 42:
 						info.pharos.ly.C_Global.g_PropACount = (info.pharos.ly.C_Global.g_PropACount + 0x1);
 						this.AddScoreMain((info.pharos.ly.C_Global.g_PropACount * 0x64));
 						this.c_Effect.CreatePropAScore_Eff(cursorX, cursorY,
 								info.pharos.ly.C_Global.g_PropACount);
 						break;
 					case 43:
 						this.c_Eidolon.CreateEidolon(param, cursorX, cursorY);
 						break;
 					case 44:
 						info.pharos.ly.C_Global.g_EidolonCount = (info.pharos.ly.C_Global.g_EidolonCount + 0x1);
 						this.AddScoreMain((info.pharos.ly.C_Global.g_EidolonCount * 0xa));
 						this.c_Effect.CreateEidolonScore_Eff(cursorX, cursorY,
 								info.pharos.ly.C_Global.g_EidolonCount);
 						break;
 					case 48:
 						this.c_EVTPowerBall.CreatePowerBall(
 								this.c_EVTPowerBall, cursorX, cursorY);
 						break;
 					case 49:
 						this.c_EVTPowerBall.EVTCLR();
 						break;
 					case 45:
 						this.setSceneDash();
 					} // end of switch
 					break;
 				case 20:
 					switch (message) {
 					case 9:
 						this.ExitEVENT();
 						info.pharos.ly.C_PUB.setGameState(12);
 						this.subGameLife();
 //						com.game.UnicornDash.C_PUB.ShowAdView(true);
 						break;
 					default:
 						info.pharos.ly.C_PUB.setGameState(message);
 					} // end of switch
 					break;
 				case 30:
 					info.pharos.ly.C_PUB.setGameState(10);
 					info.pharos.ly.C_PUB.setGameMode(message);
 					break;
 				} // end of switch
 					// :cond_2
 					// }
 				i = (i + 0x1);
 				// goto :goto_1
 				// :cond_1
 			}
 			info.pharos.ly.C_OPhoneApp.cLib.getMessageMgr()
 					.RemoveAllMsgQueue();
 		}
 		// goto :goto_0
 		// :goto_0
 		return;
 
 	}
 
 private void ReadTouch(){
 	info.pharos.ly.C_OPhoneApp.cTouch.ReadTouch();
 	info.pharos.ly.C_PUB.CHKTouch_Input();
 	info.pharos.ly.C_OPhoneApp.cLib.getInput().ReadKeyBoard();
 	if (info.pharos.ly.C_OPhoneApp.cLib.getInput().CHKUpKey(4)) {
 	if (info.pharos.ly.C_Global.g_GameState == 7) {
 	info.pharos.ly.C_Media.PlaySound(5);
 	info.pharos.ly.C_OPhoneApp.cLib.getMessageMgr().SendMessage(10,6,0);
 	// :cond_0
 	}
 	if (info.pharos.ly.C_Global.g_GameState == 11) {
 	info.pharos.ly.C_Media.PlaySound(5);
 	info.pharos.ly.C_OPhoneApp.cLib.getMessageMgr().SendMessage(10,7,0);
 	// :cond_1
 	}
 	}
 	return;
 }
 
 	private void ResultMain() {
 		boolean CountEnd = false;
 		;
 		switch (info.pharos.ly.C_Global.g_GameLife) {
 		case 0:
 			this.m_ScoreCount3 = (this.m_ScoreCount3 + this.getCountSpeed(
 					this.m_ScoreCount3,
 					info.pharos.ly.C_Global.g_Dash3Score));
 			if (this.m_ScoreCount3 > info.pharos.ly.C_Global.g_Dash3Score) {
 				this.m_ScoreCount3 = info.pharos.ly.C_Global.g_Dash3Score;
 				this.m_FinalCount = (this.m_FinalCount + this.getCountSpeed(
 						this.m_FinalCount, this.m_FinalScore));
 				if (this.m_FinalCount > this.m_FinalScore) {
 					this.m_FinalCount = this.m_FinalScore;
 					CountEnd = true;
 				}
 			}
 			break;
 		case 1:
 			this.m_ScoreCount2 = (this.m_ScoreCount2 + this.getCountSpeed(
 					this.m_ScoreCount2,
 					info.pharos.ly.C_Global.g_Dash2Score));
 			if (this.m_ScoreCount2 > info.pharos.ly.C_Global.g_Dash2Score) {
 				this.m_ScoreCount2 = info.pharos.ly.C_Global.g_Dash2Score;
 				CountEnd = true;
 			}
 			break;
 		case 2:
 			this.m_ScoreCount1 = (this.m_ScoreCount1 + this.getCountSpeed(
 					this.m_ScoreCount1,
 					info.pharos.ly.C_Global.g_Dash1Score));
 			if (this.m_ScoreCount1 > info.pharos.ly.C_Global.g_Dash1Score) {
 				this.m_ScoreCount1 = info.pharos.ly.C_Global.g_Dash1Score;
 				CountEnd = true;
 				break;
 			} // end of switch
 				// :cond_0
 		}
 		if (!CountEnd
 				&& (info.pharos.ly.C_OPhoneApp.cLib.nVBLCount % 0x3) == 0) {
 			info.pharos.ly.C_Media.PlaySound(7);
 			// :cond_1
 		}
 		info.pharos.ly.C_PUB.showPauseSCR();
 		info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().WriteSprite(
 				2130837718, 160, 240, 6);
 		if (CountEnd) {
 			if (info.pharos.ly.C_Global.g_GameLife == 0) {
 				info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas()
 						.WriteSprite(2130837721, 20, 240, 6);
 			} else {
 				info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas()
 						.WriteSprite(2130837720, 20, 240, 6);
 			}
 			// goto/16 :goto_1
 			// :goto_1
 			if ((info.pharos.ly.C_OPhoneApp.cLib.nVBLCount % 0x10) > 8) {
 				info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas()
 						.WriteSprite(2130837719, 20, 76, 6);
 				// :cond_2
 			}
 		}
 		int x = 145;
 		int y = 285;
 		;
 		switch (info.pharos.ly.C_Global.g_GameLife) {
 		case 0:
 			info.pharos.ly.C_PUB.ShowNum(this.m_ScoreCount3, (x + 0x0),
 					y, 12, 1, 5, info.pharos.ly.C_DEF.ResultNUM2TBL, 7);
 			if (this.m_FinalCount != 0) {
 				info.pharos.ly.C_PUB.ShowNum(this.m_FinalCount, 70, 175,
 						15, 1, 5, info.pharos.ly.C_DEF.ResultNUM1TBL, 7);
 				// :cond_6
 			}
 			if (this.m_FinalCount >= info.pharos.ly.C_Save.g_HighScore) {
 				info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas()
 						.WriteSprite(2130837722, 300, 240, 6);
 				// :cond_7
 			}
 		case 1:
 			info.pharos.ly.C_PUB.ShowNum(this.m_ScoreCount2, (x + 0x1c),
 					y, 12, 1, 5, info.pharos.ly.C_DEF.ResultNUM2TBL, 7);
 		case 2:
 			info.pharos.ly.C_PUB.ShowNum(this.m_ScoreCount1, (x + 0x38),
 					y, 12, 1, 5, info.pharos.ly.C_DEF.ResultNUM2TBL, 7);
 			break;
 		} // end of switch
 		info.pharos.ly.C_PUB.ShowNum(
 				info.pharos.ly.C_Save.g_HighScore, 70, 320, 15, 1, 5,
 				info.pharos.ly.C_DEF.ResultNUM1TBL, 7);
 		if (CountEnd
 				&& (info.pharos.ly.C_OPhoneApp.cTouch.CHKTouchDown() || info.pharos.ly.C_OPhoneApp.cLib
 						.getInput().CHKUpKey(4))) {
 			// :cond_3
 			this.ExitEVENT();
 			this.ClearACT();
 			info.pharos.ly.C_Media.PlaySound(5);
 			;
 			switch (info.pharos.ly.C_Global.g_GameLife) {
 			case 0:
 				info.pharos.ly.C_PUB.setGameState(10);
 				this.m_isRun = false /* 0 */;
 				break;
 			case 1:
 			case 2:
 				this.ExitEVENT();
 				info.pharos.ly.C_PUB.setGameState(1);
 				info.pharos.ly.C_OPhoneApp.cLib.ViewDark(64);
 				break;
 			} // end of switch
 //			com.game.UnicornDash.C_PUB.ShowAdView(false);
 			// :cond_4
 		}
 		return;
 		// [OTHER] end local v2 #y:I
 		// [OTHER] end local v12 #x:I
 
 		// op;
 
 	}
 
 private void SCRPullCtrl(){
 	int XVal = (((info.pharos.ly.C_Global.g_SceneFloat_X * 0x2) / 0x4) - 200);
 	if (XVal < -280) {
 	XVal = -280;
 	// :cond_0
 	}
 	if (XVal > 0) {
 	XVal = 0 /* 0 */;
 	// :cond_1
 	}
 	info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().SetTextXVal(0,(XVal - (info.pharos.ly.C_Global.g_SceneDash_X / 0x2)));
 	info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().SetTextYVal(0,(-48 - (info.pharos.ly.C_Global.g_SceneDash_Y / 0x2)));
 	return;
 }
 
 private void SceneDashCtrl(){
 	info.pharos.ly.C_Global.g_SceneDash_X = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_SceneDash_Y = 0 /* 0 */;
 	if (info.pharos.ly.C_Global.g_SceneDash_Idx > 0) {
 	info.pharos.ly.C_Global.g_SceneDash_X = info.pharos.ly.C_SceneNormal.SceneDashTBL[info.pharos.ly.C_Global.g_SceneDash_Idx];
 	info.pharos.ly.C_Global.g_SceneDash_Y = info.pharos.ly.C_SceneNormal.SceneDashTBL[info.pharos.ly.C_Global.g_SceneDash_Idx];
 	info.pharos.ly.C_Global.g_SceneDash_Idx = (info.pharos.ly.C_Global.g_SceneDash_Idx - 1);
 	// :cond_0
 	}
 	return;
 }
 
 private void SceneFloatCtrl(){
 	if (!this.c_EVTPlayer.EVT.Valid) {
 	// :cond_0
 	} else {
 	if (this.c_EVTPlayer.EVT.Ctrl != 4) {
 	int Show_X = ((this.c_EVTPlayer.EVT.XVal >> 0x10) + info.pharos.ly.C_Global.g_SceneFloat_X);
 	int Cur_X = (this.c_EVTPlayer.EVT.XVal >> 0x10);
 	int XInc = (this.c_EVTPlayer.EVT.XInc >> 0x10);
 	if (this.c_EVTPlayer.EVT.Valid) {
 	;
 	switch(this.c_EVTPlayer.EVT.Ctrl){
 	case 1:  case 2: 
 	if (Show_X < 0) {
 	Show_X = 0 /* 0 */;
 	// :cond_5
 	}
 	int Rate = (Show_X / 0xa);
 	
 	if (info.pharos.ly.C_Global.g_SceneFloat_X > 250) {
 	Rate = ((info.pharos.ly.C_Global.g_SceneFloat_X - 250) / 0xa);
 	// :cond_6
 	}
 	if (Rate >= 64) {
 	Rate = 63;
 	// :cond_7
 	}
 	info.pharos.ly.C_Global.g_SceneFloat_X = (info.pharos.ly.C_Global.g_SceneFloat_X - (int)((double)(this.c_EVTPlayer.EVT.XInc >> 0x10) * info.pharos.ly.C_SceneNormal.ScaleTBL[Rate]));
 	break;
 	case 3:
 		break;
 	default:
 	Cur_X = (Cur_X + info.pharos.ly.C_Global.g_SceneFloat_X);
 	Rate = (Math.abs((Cur_X - 140)) / 0xa);
 	if (Cur_X > 145) {
 	info.pharos.ly.C_Global.g_SceneFloat_X = (info.pharos.ly.C_Global.g_SceneFloat_X - Rate);
 	// :cond_2
 	}
 	if (Cur_X < 135) {
 	info.pharos.ly.C_Global.g_SceneFloat_X = (info.pharos.ly.C_Global.g_SceneFloat_X + Rate);
 	//[OTHER] end local v1           #Rate:I
 	// :cond_3
 	}
 	} //end of switch
 	//:pswitch_0
 	if (Show_X > 300) {
 	info.pharos.ly.C_Global.g_SceneFloat_X = (info.pharos.ly.C_Global.g_SceneFloat_X - (Show_X - 300));
 	// :cond_4
 	}
 	if (info.pharos.ly.C_Global.g_SceneFloat_X > 400) {
 	info.pharos.ly.C_Global.g_SceneFloat_X = 400;
 	//goto somewhere; //maybe return,continue,break: goto :goto_0
 	return;
 	// op;
 	
 		}
 		}
 		}
 		}
 	// :goto_0
 	return;
 }
 
 private void ShowEVENT(){
 	if (this.c_EVTPlayer != null) {
 	this.c_EVTPlayer.EVT.Status = (this.c_EVTPlayer.EVT.Status | 0x800);
 	this.c_EVTPlayer.EVT.DispX = (((this.c_EVTPlayer.EVT.XVal >> 0x10) + info.pharos.ly.C_Global.g_SceneFloat_X) + -0x1e);
 	this.c_EVTPlayer.EVT.DispY = (((this.c_EVTPlayer.EVT.YVal >> 0x10) + info.pharos.ly.C_Global.g_SceneFloat_Y) + 0x0);
 	this.c_EVTPlayer.ShowEVENT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
 	// :cond_0
 	}
 	if (this.c_EVTPowerBall != null && this.c_EVTPowerBall.EVT.Valid) {
 		if (this.c_EVTPowerBall.EVT.Ctrl == 0) {
 		this.c_EVTPowerBall.EVT.Status = (this.c_EVTPowerBall.EVT.Status | 0x800);
 		this.c_EVTPowerBall.EVT.DispX = (((this.c_EVTPowerBall.EVT.XVal >> 0x10) + info.pharos.ly.C_Global.g_SceneFloat_X) + -0x1e);
 		this.c_EVTPowerBall.EVT.DispY = (((this.c_EVTPowerBall.EVT.YVal >> 0x10) + info.pharos.ly.C_Global.g_SceneFloat_Y) + 0x0);
 		} else {
 		int Offset_Y = 0;
 		if (this.c_EVTPlayer.EVT.Ctrl == 3 && (this.c_EVTPlayer.EVT.Status & 32768) != 32768) {
 		Offset_Y = (info.pharos.ly.C_SceneNormal.PowerBall_Dash_OffsetTBL[this.c_EVTPlayer.EVT.CurFRM] * 0xa);
 		this.c_EVTPowerBall.EVT.ACTIdx = 2130837688;
 		// :cond_9
 		}
 		this.c_EVTPowerBall.EVT.DispX = (this.c_EVTPlayer.EVT.DispX + 0x19);
 		this.c_EVTPowerBall.EVT.DispY = ((this.c_EVTPlayer.EVT.DispY + Offset_Y) + 0x5);
 			}
 	// goto/16 :goto_0
 	// :goto_0
 	this.c_EVTPowerBall.ShowEVENT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
 	// :cond_1
 	}
 	
 	if (this.c_EVTLand != null) {
 	int i = 0;
 	while (i < 5) {
 		if (this.c_EVTLand[i].EVT.Valid) {
 		this.c_EVTLand[i].EVT.Status = (this.c_EVTLand[i].EVT.Status | 0x800);
 		this.c_EVTLand[i].EVT.DispX = ((((this.c_EVTLand[i].EVT.XVal >> 0x10) + info.pharos.ly.C_Global.g_SceneFloat_X) + info.pharos.ly.C_Global.g_SceneDash_X) + -0x1e);
 		this.c_EVTLand[i].EVT.DispY = ((((this.c_EVTLand[i].EVT.YVal >> 0x10) + info.pharos.ly.C_Global.g_SceneFloat_Y) + info.pharos.ly.C_Global.g_SceneDash_Y) + 0x0);
 		int Idx = 0;
 		int Ctrl = this.c_EVTLand[i].EVT.Ctrl;
 		while (true) {
 			int Act = this.c_EVTLand[i].EVT.ACTPtr[Ctrl][Idx];
 			if(Act == -1){break;}
 			if (this.chkACTIsShow_Y(Act,this.c_EVTLand[i].EVT.DispY)) {
 			if (Ctrl == 6) {
 				info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().WriteSprite(Act,this.c_EVTLand[i].EVT.DispX,this.c_EVTLand[i].EVT.DispY,this.c_EVTLand[i].EVT.Attrib);
 			// goto :goto_7
 			//[OTHER] end local v0           #Act:I
 			//[OTHER] end local v1           #Ctrl:I
 			//[OTHER] end local v2           #Idx:I
 			// :cond_d
 			}else{
 				if (this.chkACTIsShow_X(Act,this.c_EVTLand[i].EVT.DispX)) {
 					info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().WriteSprite(Act,this.c_EVTLand[i].EVT.DispX,this.c_EVTLand[i].EVT.DispY,this.c_EVTLand[i].EVT.Attrib);
 				}
 			}
 			}
 			// :goto_7
 			Idx = (Idx + 0x1);
 		// goto :goto_6
 		// :cond_c
 		}
 		//[OTHER] end local v0           #Act:I
 		//[OTHER] end local v1           #Ctrl:I
 		//[OTHER] end local v2           #Idx:I
 		// :cond_b
 		}
 		i = (i + 0x1);
 		// goto/16 :goto_1
 	//continue someWhile; //if (i < 5) {
 	// :cond_a
 	}
 	//[OTHER] end local v4           #i:I
 	// :cond_2
 	}
 	if (this.c_EVTPropA != null) {
 	int i = 0 /* 0 */;
 	
 	while (i < 5) {
 	this.c_EVTPropA[i].EVT.Status = (this.c_EVTPropA[i].EVT.Status | 0x800);
 	this.c_EVTPropA[i].EVT.DispX = ((((this.c_EVTPropA[i].EVT.XVal >> 0x10) + info.pharos.ly.C_Global.g_SceneFloat_X) + info.pharos.ly.C_Global.g_SceneDash_X) + -0x1e);
 	this.c_EVTPropA[i].EVT.DispY = ((((this.c_EVTPropA[i].EVT.YVal >> 0x10) + info.pharos.ly.C_Global.g_SceneFloat_Y) + info.pharos.ly.C_Global.g_SceneDash_Y) + 0x0);
 	this.c_EVTPropA[i].ShowEVENT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
 	i = (i + 0x1);
 	// goto/16 :goto_2
 	// :cond_f
 	}
 	//[OTHER] end local v4           #i:I
 	// :cond_3
 	}
 	if (this.c_EVTGameBTN != null) {
 	int i = 0 /* 0 */;
 	
 	while (i < 5) {
 	this.c_EVTGameBTN[i].ShowEVENT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
 	i = (i + 0x1);
 	// goto/16 :goto_3
 	// :cond_10
 	}
 	//[OTHER] end local v4           #i:I
 	// :cond_4
 	}
 	if (this.c_EVTEidolon != null) {
 	int i = 0 /* 0 */;
 	
 	while (i < 2) {
 	this.c_EVTEidolon[i].EVT.Status = (this.c_EVTEidolon[i].EVT.Status | 0x800);
 	this.c_EVTEidolon[i].EVT.DispX = ((((this.c_EVTEidolon[i].EVT.XVal >> 0x10) + info.pharos.ly.C_Global.g_SceneFloat_X) + info.pharos.ly.C_Global.g_SceneDash_X) + -0x1e);
 	this.c_EVTEidolon[i].EVT.DispY = ((((this.c_EVTEidolon[i].EVT.YVal >> 0x10) + info.pharos.ly.C_Global.g_SceneFloat_Y) + info.pharos.ly.C_Global.g_SceneDash_Y) + 0x0);
 	this.c_EVTEidolon[i].ShowEVENT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
 	i = (i + 0x1);
 	// goto/16 :goto_4
 	// :cond_11
 	}
 	//[OTHER] end local v4           #i:I
 	// :cond_5
 	}
 	
 	
 	if (this.c_EVTEffect != null) {
 	int i = 0 /* 0 */;
 	
 	while (i < 10) {
 		
 		switch(this.c_EVTEffect[i].EVT.Ctrl){
 		case 0:  case 3: 
 			this.c_EVTEffect[i].EVT.Status = (this.c_EVTEffect[i].EVT.Status | 0x800);
 			this.c_EVTEffect[i].EVT.DispX = ((((this.c_EVTEffect[i].EVT.XVal >> 0x10) + info.pharos.ly.C_Global.g_SceneFloat_X) + info.pharos.ly.C_Global.g_SceneDash_X) + -0x1e);
 			this.c_EVTEffect[i].EVT.DispY = ((((this.c_EVTEffect[i].EVT.YVal >> 0x10) + info.pharos.ly.C_Global.g_SceneFloat_Y) + info.pharos.ly.C_Global.g_SceneDash_Y) + 0x0);
 			break;
 		} //end of switch
 		
 		this.c_EVTEffect[i].ShowEVENT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
 		i = (i + 0x1);
 		
 		
 	// :cond_6
 	}
 	if (info.pharos.ly.C_Global.g_GameState == 7) {
 		this.c_DashBTN.ShowEVENT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
 		this.c_JumpBTN.ShowEVENT(info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas());
 		// :cond_7
 		}
 		return;
 	}
 	//[OTHER] end local v3           #Offset_Y:I
 	
 	
 	
 	
 }
 
 private void ShowLife(){
 	int x = 300;
 	int y = (info.pharos.ly.C_Global.g_ScreenScale_3_1 + 0x17c);
 	int s = 30;
 	int i = 0;
 	while (i < 3) {
 	if (info.pharos.ly.C_Global.g_GameLife > i) {
 	info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().WriteSprite(2130837515,x,((i * s) + y),5);
 	} else {
 	info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().WriteSprite(2130837516,x,((i * s) + y),5);
 		}
 	// goto :goto_1
 	// :goto_1
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_0
 	}
 	return;
 }
 
 private void ShowScore(){
 	if (info.pharos.ly.C_Global.g_GameState == 7) {
 	info.pharos.ly.C_Global.g_RunLength = (info.pharos.ly.C_Global.g_RunLength + (long)this.getLandSpeed());
 	// :cond_0
 	}
 	info.pharos.ly.C_PUB.ShowNum(this.getGameScore(),300,240,15,0,5,info.pharos.ly.C_DEF.RunLengthNumTBL,5);
 	return;
 }
 
 private boolean chkACTIsShow_X(int Act,int XVal){
 	int XHitL = info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().GetSpriteXHitL(Act);
 	int XHitR = info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().GetSpriteXHitR(Act);
 	int X1 = (XVal - XHitL);
 	int X2 = (XVal + XHitR);
 	if (X1 >= 0 && X1 <= 320) {
 	;
 	return true /* 1 */;
 	// :cond_0
 	} else if (X2 >= 0 && X2 <= 320) {
 	;
 	return true /* 1 */;
 	// goto :goto_0
 	} else {
 	;
 	return false /* 0 */;
 		}
 	// goto :goto_0
 	// :goto_0
 	//return 1;
 }
 
 private boolean chkACTIsShow_Y(int Act,int YVal){
 		int Area = ((info.pharos.ly.C_Global.g_scrHeight - 480) / 0x2);
 		int YHitU = info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas()
 				.GetSpriteYHitU(Act);
 		int YHitD = info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas()
 				.GetSpriteYHitD(Act);
 		int Y1 = (YVal + YHitD);
 		int Y2 = (YVal - YHitU);
 		// com.game.UnicornDash.C_OPhoneApp.cLib.getGameCanvas() = -Area;
 		if (Y1 >= -Area && Y1 <= (Area + 0x1e0)) {
 			;
 			return true /* 1 */;
 		} else {
 			// 1 = -Area;
 			if (Y2 >= -1 && Y2 <= (Area + 0x1e0)) {
 				;
 				return true /* 1 */;
 				// goto :goto_0
 			} else {
 				;
 				return false /* 0 */;
 			}
 		}
 	// goto :goto_0
 	// :goto_0
 	//return 1;
 }
 
 private int getCountSpeed(int CurScore,int TarScore){
 	int Speed = 1;
 	if ((TarScore - CurScore) > 10) {
 	Speed = (Speed + 0x1);
 	// :cond_0
 	}
 	if ((TarScore - CurScore) > 100) {
 	Speed = (Speed + 0xa);
 	// :cond_1
 	}
 	if ((TarScore - CurScore) > 1000) {
 	Speed = (Speed + 0x64);
 	// :cond_2
 	}
 	if ((TarScore - CurScore) > 10000) {
 	Speed = (Speed + 0x3e8);
 	// :cond_3
 	}
 	if (info.pharos.ly.C_PUB.CHKisTouch()) {
 	;
 	return Speed;
 	} else {
 	;
 	return Speed;
 		}
 	// goto :goto_0
 	// :goto_0
 	//return Speed;
 }
 
 private int getGameScore(){
 	;
 	return (int)((info.pharos.ly.C_Global.g_RunLength >> 16) / 5);
 	//return (int)((com.game.UnicornDash.C_Global.g_RunLength >> 16) / 5);
 }
 
 private int getLandSpeed(){
 	;
 	return (info.pharos.ly.C_Global.g_LandMoveSpeed + info.pharos.ly.C_Global.g_DashSpeedArea);
 	//return (com.game.UnicornDash.C_Global.g_LandMoveSpeed + com.game.UnicornDash.C_Global.g_DashSpeedArea);
 }
 
 private void setSceneDash(){
 	info.pharos.ly.C_Global.g_SceneDash_Idx = 20;
 	return;
 }
 
 private void showHightRecord(){
 	if (info.pharos.ly.C_Global.g_GameState != 7) {
 	// :cond_0
 	} else {
 	info.pharos.ly.C_Global.g_CurDistance = (info.pharos.ly.C_Global.g_CurDistance + (long)this.getLandSpeed());
 	int RunLength = (int)(info.pharos.ly.C_Global.g_CurDistance >> 16);
 	int YVal = (info.pharos.ly.C_Save.g_HighDistance - RunLength);
 	if (info.pharos.ly.C_Save.g_HighDistance == 0 || YVal < -48 || YVal > 576) {
 	info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().WriteSprite(2130837537,0,YVal,5);
 	info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().WriteSprite(2130837538,160,YVal,5);
 		}
 		}
 	// goto :goto_0
 	// :goto_0
 	return;
 }
 
 private void subGameLife(){
 	int CurDistance = (int)(info.pharos.ly.C_Global.g_RunLength >> 16);
 	if (CurDistance > info.pharos.ly.C_Save.g_HighDistance) {
 	info.pharos.ly.C_Save.g_HighDistance = CurDistance;
 	// :cond_0
 	}
 	if (info.pharos.ly.C_Global.g_GameLife != 0) {
 	info.pharos.ly.C_Global.g_GameLife = (info.pharos.ly.C_Global.g_GameLife - 1);
 	// :cond_1
 	}
 	;
 	switch(info.pharos.ly.C_Global.g_GameLife){
 	case 0: 
 	this.m_ScoreCount1 = info.pharos.ly.C_Global.g_Dash1Score;
 	this.m_ScoreCount2 = info.pharos.ly.C_Global.g_Dash2Score;
 	this.m_ScoreCount3 = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_Dash3Score = this.getGameScore();
 	this.m_FinalScore = ((info.pharos.ly.C_Global.g_Dash1Score + info.pharos.ly.C_Global.g_Dash2Score) + info.pharos.ly.C_Global.g_Dash3Score);
 	if (this.m_FinalScore > info.pharos.ly.C_Save.g_HighScore) {
 	info.pharos.ly.C_Save.g_HighScore = this.m_FinalScore;
 	}
 	break;
 	case 1: 
 	this.m_ScoreCount1 = info.pharos.ly.C_Global.g_Dash1Score;
 	this.m_ScoreCount2 = 0 /* 0 */;
 	this.m_ScoreCount3 = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_Dash2Score = this.getGameScore();
 	break;
 	case 2: 
 	this.m_ScoreCount1 = 0 /* 0 */;
 	this.m_ScoreCount2 = 0 /* 0 */;
 	this.m_ScoreCount3 = 0 /* 0 */;
 	info.pharos.ly.C_Global.g_Dash1Score = this.getGameScore();
 	break;
 	} //end of switch
 	// :cond_2
 	this.m_FinalCount = 0 /* 0 */;
 	info.pharos.ly.C_Save.UpdataData();
 	return;
 	// op;
 	
 }
 // virtual methods
 
 public void ExitEVENT(){
 	if (this.c_EVTPlayer != null) {
 	this.c_EVTPlayer.EVTCLR();
 	// :cond_0
 	}
 	if (this.c_EVTLand != null) {
 	int i = 0;
 	while (i < 5) {
 	this.c_EVTLand[i].EVTCLR();
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_5
 	}
 	//[OTHER] end local v0           #i:I
 	// :cond_1
 	}
 	if (this.c_EVTGameBTN != null) {
 	int i = 0 /* 0 */;
 	
 	while (i < 5) {
 	this.c_EVTGameBTN[i].EVTCLR();
 	i = (i + 0x1);
 	// goto :goto_1
 	// :cond_6
 	}
 	//[OTHER] end local v0           #i:I
 	// :cond_2
 	}
 	if (this.c_EVTPropA != null) {
 	int i = 0 /* 0 */;
 	
 	while (i < 5) {
 	this.c_EVTPropA[i].EVTCLR();
 	i = (i + 0x1);
 	// goto :goto_2
 	// :cond_7
 	}
 	//[OTHER] end local v0           #i:I
 	// :cond_3
 	}
 	if (this.c_EVTEidolon != null) {
 	int i = 0 /* 0 */;
 	
 	while (i < 2) {
 	this.c_EVTEidolon[i].EVTCLR();
 	i = (i + 0x1);
 	// goto :goto_3
 	// :cond_8
 	}
 	//[OTHER] end local v0           #i:I
 	// :cond_4
 	}
 	return;
 	
 }
 
 	public void GameMain() {
 		this.Initialize();
 		while (this.m_isRun) {
 			// this.ExitEVENT();
 			// com.game.UnicornDash.C_OPhoneApp.cLib.ViewDark(64);
 			// com.game.UnicornDash.C_OPhoneApp.cLib.getMessageMgr().SendMessage(0,30,5);
 			// return;
 			// } else {
 			this.ClearACT();
 			this.ReadTouch();
 			;
 			switch (info.pharos.ly.C_Global.g_GameState) {
 			case 1:
 				this.InitAgain();
 				info.pharos.ly.C_PUB.setGameState(3);
 				break;
 			case 3:
 				info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas().LoadText(
 						info.pharos.ly.C_DEF.SceSceneTBL[this.m_SCRIdx],
 						0, 0);
 				info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas()
 						.SetTextYVal(0, -48);
 				this.SCRPullCtrl();
 				this.ShowEVENT();
 				this.ShowScore();
 				this.ShowLife();
 				info.pharos.ly.C_OPhoneApp.cLib.ViewOpen(64);
 				info.pharos.ly.C_PUB.setGameState(7);
 				break;
 			case 4:
 				this.InitAgain();
 				info.pharos.ly.C_PUB.setGameState(7);
 				break;
 			case 7:
 				this.LandMain();
 				this.SceneFloatCtrl();
 				this.SCRPullCtrl();
 				this.MakeHorseEff();
 				this.c_Effect.CreateCloud_Eff(160, 240, this.getLandSpeed());
 				break;
 			case 11:
 				info.pharos.ly.C_PUB.showPauseSCR();
 				info.pharos.ly.C_OPhoneApp.cLib.getGameCanvas()
 						.WriteSprite(2130837677, 260, 240, 6);
 				break;
 			case 12:
 				this.ResultMain();
 				break;
 			case 10:
 				this.m_isRun = false /* 0 */;
 				break;
 			} // end of switch
 				// :pswitch_0
 			this.ShowScore();
 			this.ShowLife();
 			this.showHightRecord();
 			this.SceneDashCtrl();
 			this.MsgLoop();
 			this.ExecEVENT();
 			this.ShowEVENT();
 			info.pharos.ly.C_OPhoneApp.cLib.WaitBLK();
 			// goto somewhere; //maybe return,continue,break: goto :goto_0
 			// op;
 
 		}
 
 		this.ExitEVENT();
 		info.pharos.ly.C_OPhoneApp.cLib.ViewDark(64);
 		info.pharos.ly.C_OPhoneApp.cLib.getMessageMgr().SendMessage(0,
 				30, 5);
 	}
 
 }

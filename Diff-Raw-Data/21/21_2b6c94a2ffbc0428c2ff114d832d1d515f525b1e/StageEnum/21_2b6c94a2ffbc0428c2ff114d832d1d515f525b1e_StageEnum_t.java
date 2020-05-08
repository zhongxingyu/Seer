 package ch.gibb.project.enums;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import android.graphics.PointF;
 import android.graphics.RectF;
 import android.util.DisplayMetrics;
 
 public enum StageEnum {
 
 	// @formatter:off
 	//new PointF(dpFromPx(600), dpFromPx(1180)),  -> Error
 	STAGE_1(new PointF(600, 1180), 
 			new PointF(600, 1023), 
 			new PointF[] {	new PointF(612, 40), 
 							new PointF(539, 40), 
 							new PointF(466, 40),
 							new PointF(393, 40), 
 							new PointF(320, 40), 
 							new PointF(247, 40),
							new PointF(592, 113), 
							new PointF(539, 186), 
							new PointF(539, 259),
 							new PointF(320, 332), 
 							new PointF(103, 332), 
 							new PointF(186, 478),
 							new PointF(395, 551), 
 							new PointF(40, 1172), 
 							new PointF(40, 1099),
 							new PointF(40, 1026), 
 							new PointF(40, 953), 
 							new PointF(176, 1026),
 							new PointF(176, 953), 
 							new PointF(612, 880), 
 							new PointF(176, 880),
 							new PointF(176, 807), 
 							new PointF(120, 750) }, 
 			new ArrayList<PointF>(Arrays.asList( 
							new PointF(50, 50), 
 							new PointF(320, 259), 
 							new PointF(320, 405),
 							new PointF(193, 740), 
							new PointF(612, 186))), 
 			new RectF[] {
 							new RectF(113, 40, 164, 320), 
 							new RectF(119, 480, 170, 740),
 							new RectF(329, 963, 685, 1014), 
 							new RectF(189, 1109, 685, 1160),
 							new RectF(259, 250, 310, 559), 
 							new RectF(405, 200, 456, 539),
 							new RectF(456, 450, 580, 501), 
 							new RectF(529, 501, 580, 801),
 							new RectF(405, 700, 456, 963) }),
 							
 	STAGE_2(new PointF(40, 40), 
 			new PointF(322, 1006), 
 			new PointF[] {	
 							new PointF(612, 40), 
 							new PointF(466, 40),
 							new PointF(393, 40),
 							new PointF(527, 220),
 							new PointF(527, 293),
 							new PointF(527, 366),
 							new PointF(527, 439),
 							new PointF(216, 164),
 							new PointF(216, 237),
 							new PointF(159, 500),
 							new PointF(216, 600),
 							new PointF(40, 870),
 							new PointF(40, 943),
 							new PointF(40, 1016),
 							new PointF(40, 1089),
 							new PointF(40, 1162),
 							new PointF(166, 800),
 							new PointF(166, 870),
 							new PointF(166, 943),
 							new PointF(166, 1016),
 							new PointF(400, 1079),
 							new PointF(400, 1006),
 							new PointF(400, 933),
 							new PointF(327, 933),
 							new PointF(400, 860),
 							new PointF(400, 787),
 							new PointF(523, 1079),
 							new PointF(523, 1006),
 							new PointF(523, 933),
 							new PointF(523, 860),
 							new PointF(523, 787),
 							new PointF(523, 714),
 							new PointF(523, 641)
 			}, 
 			new ArrayList<PointF>(Arrays.asList( 
 						new PointF(539, 40),
 						new PointF(159, 680),
 						new PointF(226, 680),
 						new PointF(596, 714),
 						new PointF(596, 641))),
 			new RectF[] {
 							new RectF(40, 113, 340, 164), 
 							new RectF(289, 164, 600, 215),
 							new RectF(340, 565, 685, 616),
 							new RectF(409, 335, 460, 565),
 							new RectF(289, 215, 340, 405),
 							new RectF(108, 245, 159, 800),
 							new RectF(289, 565, 340, 800),
 							new RectF(159, 749, 289, 800),
 							new RectF(166, 1089, 400, 1140),
 							}),
 							
 	STAGE_3(new PointF(381, 1180), 
 			new PointF(240, 1160), 
 			new PointF[] {	
 							new PointF(602, 972),
 							new PointF(530, 972),
 							new PointF(458, 972),
 							new PointF(458, 900),
 							new PointF(458, 828),
 							new PointF(400, 705),
 							new PointF(473, 705),
 							new PointF(546, 705),
 							new PointF(546, 633),
 							new PointF(400, 561),
 							new PointF(400, 634),
 							new PointF(542, 422),
 							new PointF(400, 422),
 							new PointF(602, 299),
 							new PointF(530, 299),
 							new PointF(530, 226),
 							new PointF(530, 153),
 							new PointF(530, 80),
 							new PointF(458, 299),
 							new PointF(400, 206),
 							new PointF(400, 133),
 							new PointF(257, 100),
 							new PointF(184, 100),
 							new PointF(111, 100),
 							new PointF(400, 133),
 							new PointF(400, 133),
 							new PointF(186, 223),
 							new PointF(113, 223),
 							new PointF(40, 223),
 							new PointF(257, 346),
 							new PointF(184, 346),
 							new PointF(111, 346),
 							new PointF(100, 546),
 							new PointF(100, 619),
 							new PointF(210, 682),
 							new PointF(45, 805),
 							new PointF(118, 805),
 							new PointF(191, 805),
 							new PointF(45, 1039),
 							new PointF(45, 1112),
 							new PointF(168, 1090),
 							new PointF(241, 1090)
 			}, 
 			new ArrayList<PointF>(Arrays.asList( 
 							new PointF(530, 900),
 							new PointF(473, 633),
 							new PointF(602, 226),
 							new PointF(184, 426),
 							new PointF(257, 426),
 							new PointF(173, 619),
 							new PointF(241, 1017)
 						)),
 			new RectF[] {
 							new RectF(330, 100, 381, 1240),
 							new RectF(381, 495, 615, 546),
 							new RectF(100, 495, 330, 546),
 							new RectF(381, 1095, 615, 1146),
 							new RectF(531, 828, 615, 879),
 							new RectF(100, 692, 205, 743),
 							new RectF(100, 928, 330, 979)
 							}),
 							
 	STAGE_4(new PointF(300, 1177), 
 			new PointF(435, 1157), 
 			new PointF[] {	
 							new PointF(40, 987),
 							new PointF(40, 914),
 							new PointF(40, 841),
 							new PointF(40, 768),
 							new PointF(40, 695),
 							new PointF(40, 622),
 							new PointF(40, 549),
 							new PointF(40, 476),
 							new PointF(40, 403),
 							new PointF(40, 330),
 							new PointF(40, 257),
 							new PointF(40, 184),
 							new PointF(40, 111),
 							new PointF(40, 40),
 							
 							new PointF(163, 1050),
 							new PointF(163, 977),
 							new PointF(163, 904),
 							new PointF(163, 831),
 							new PointF(163, 758),
 							new PointF(163, 685),
 							new PointF(163, 612),
 							new PointF(163, 539),
 							new PointF(163, 466),
 							new PointF(163, 393),
 							new PointF(163, 320),
 							new PointF(163, 247),
 							new PointF(163, 174),
 		
 							new PointF(286, 184),
 							new PointF(359, 184),
 							new PointF(432, 184),
 							new PointF(505, 184),
 							new PointF(578, 184),
 							new PointF(286, 111),
 							new PointF(286, 40),
 							
 							new PointF(236, 320),
 							new PointF(309, 320),
 							new PointF(382, 320),
 							new PointF(455, 320),
 							new PointF(528, 320),
 							
 							new PointF(610, 466),
 							new PointF(537, 466),
 							new PointF(464, 466),
 							new PointF(391, 466),
 							new PointF(318, 466),
 							
 							new PointF(280, 526),
 							new PointF(280, 599),
 							new PointF(280, 672),
 							new PointF(280, 745),
 							new PointF(280, 818),
 							new PointF(280, 891),
 							new PointF(280, 964),
 							
 							new PointF(403, 1050),
 							new PointF(403, 969),
 							new PointF(403, 896),
 							new PointF(403, 823),
 							new PointF(403, 750),
 							new PointF(403, 614),
 							new PointF(476, 614),
 							new PointF(549, 614),
 							
 							new PointF(549, 687),
 							new PointF(549, 760),
 							new PointF(549, 833),
 							new PointF(549, 906),
 							new PointF(549, 979),
 							new PointF(549, 1052)
 							
 			}, 
 			new ArrayList<PointF>(Arrays.asList( 
 							new PointF(359, 111),
 							new PointF(359, 40),
 							new PointF(476, 979),
 							new PointF(476, 1052)
 					)),
 			new RectF[] {
 							new RectF(370, 1125, 421, 1240),
 							new RectF(100, 1125, 370, 1176),
 							new RectF(421, 1125, 615, 1140)
 							});
 	
 	// @formatter:on
 
 	private PointF startPoint;
 	private PointF finishPoint;
 	private PointF[] holes;
 	private ArrayList<PointF> stars;
 	private RectF[] walls;
 	private static DisplayMetrics metrics;
 
 	StageEnum(PointF startPoint, PointF finishPoint, PointF[] holes,
 			ArrayList<PointF> stars, RectF[] walls) {
 		this.startPoint = startPoint;
 		this.finishPoint = finishPoint;
 		this.holes = holes;
 		this.stars = stars;
 		this.walls = walls;
 	}
 
 	// TODO: Implement this function, => Display Density relative
 	public static float dpFromPx(float px) {
 		float dp = px / (metrics.densityDpi / 320f);
 		return dp;
 	}
 
 	public PointF getStartPoint() {
 		return startPoint;
 	}
 
 	public PointF getFinishPoint() {
 		return finishPoint;
 	}
 
 	public PointF[] getHoles() {
 		return holes;
 	}
 
 	public ArrayList<PointF> getStars() {
 		return stars;
 	}
 
 	public RectF[] getWalls() {
 		return walls;
 	}
 
 	public static void setDisplayMetrics(DisplayMetrics displayMetrics) {
 		metrics = displayMetrics;
 	}
 }

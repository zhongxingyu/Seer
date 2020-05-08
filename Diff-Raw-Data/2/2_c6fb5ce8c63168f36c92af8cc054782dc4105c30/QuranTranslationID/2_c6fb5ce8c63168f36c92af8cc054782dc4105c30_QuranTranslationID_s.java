 package info.usmans.QuranProject.controller;
 
 public enum QuranTranslationID {
 
 	UR_MAUDUDI {
 		@Override
 		public String getResourcePath() {
 			return "/info/usmans/QuranProject/resources/trans/ur.maududi.xml";
 		}
 	},
 	UR_AHMED_RAZA_KHAN {
 
 		@Override
 		public String getResourcePath() {
 			return "/info/usmans/QuranProject/resources/trans/ur.kanzuliman.xml";
 		}
 
 	},
 	UR_JALANDHRY {
 
 		@Override
 		public String getResourcePath() {
 			return "/info/usmans/QuranProject/resources/trans/ur.jalandhry.xml";
 		}
 		
 	},
 	UR_AHMEDALI {
 		@Override
 		public String getResourcePath() {
 			return "/info/usmans/QuranProject/resources/trans/ur.ahmedali.xml";
 		}
 	},
 	UR_QADRI {
 		@Override
 		public String getResourcePath() {
			return "../resources/trans/ur.qadri.xml";
 		}
 	},
 	UR_JAWADI {
 		@Override
 		public String getResourcePath() {
 			return "/info/usmans/QuranProject/resources/trans/ur.jawadi.xml";
 		}
 	},
 	UR_JUNAGARHI {
 		@Override
 		public String getResourcePath() {
 			return "/info/usmans/QuranProject/resources/trans/ur.junagarhi.xml";
 		}
 	},
 	UR_NAJAFI {
 		@Override
 		public String getResourcePath() {
 			return "/info/usmans/QuranProject/resources/trans/ur.najafi.xml";
 		}
 	};
 
 	public abstract String getResourcePath();
 
 	@Override
 	public String toString() {
 		switch (this) {
 		case UR_MAUDUDI:
 			return "ابوالاعلی مودودی";
 		case UR_AHMED_RAZA_KHAN:
 			return "احمد رضا خان";
 		case UR_JALANDHRY:
 			return "فتح محمّد جالندھری";
 		case UR_AHMEDALI:
 			return "احمد علی";
 		case UR_QADRI:
 			return "طاہر القادری";
 		case UR_JAWADI:
 			return "علامہ جوادی";
 		case UR_JUNAGARHI:
 			return "محمد جوناگڑھی";
 		case UR_NAJAFI:
 			return "محمد حسین نجفی";
 
 		}
 		return super.toString();
 	}
 
 }

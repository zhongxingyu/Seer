 package net.juniorbl.jtoyracing.util;
 
 /**
  * Paths of the resources.
  *
  * @version 1.0 Jun 22, 2007
  * @author Fco. Carlos L. Barros Junior
  */
 public enum ResourcesPath {
 
 	/**
 	 * Path of the models.
 	 */
 	MODELS_PATH {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public String toString() {
			return "resources/models/";
 		}
 	},
 	
 	/**
 	 * Path of the audio.
 	 */
 	AUDIO_PATH {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public String toString() {
			return "resources/audio/";
 		}
 	},
 	
 	/**
 	 * Path of the images.
 	 */
 	IMAGES_PATH {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public String toString() {
			return "resources/images/";
 		}
 	},
 	
 	/**
 	 * Path of the texture.
 	 */
 	TEXTURE_PATH {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public String toString() {
			return "resources/textures/";
 		}
 	}
 }

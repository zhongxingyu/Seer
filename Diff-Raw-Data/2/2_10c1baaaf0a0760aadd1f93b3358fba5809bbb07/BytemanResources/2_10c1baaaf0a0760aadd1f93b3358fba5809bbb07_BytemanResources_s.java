 /*
  * Copyright (c) 2011-2012 Julien Nicoulaud <julien.nicoulaud@gmail.com>
  *
  * This file is part of idea-byteman.
  *
  * idea-byteman is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * idea-byteman is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with idea-byteman.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.nicoulaj.idea.byteman;
 
 import com.intellij.CommonBundle;
 import com.intellij.openapi.util.IconLoader;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.PropertyKey;
 
 import javax.swing.*;
 import java.util.ResourceBundle;
 
 /**
 * Graphic resources for the Byteman language support plugin.
  *
  * @author <a href="mailto:julien.nicoulaud@gmail.com">Julien Nicoulaud</a>
  * @since 0.1
  */
 public class BytemanResources {
 
     /** Graphic resources for the Byteman language support plugin. */
     public static class Graphics {
 
         /** {@link net.nicoulaj.idea.byteman.BytemanResources.Graphics} is a non-instantiable static class. */
         private Graphics() {}
 
         /** Path to Byteman graphic resources. */
         public static final String BYTEMAN_GRAPHICS_PATH = "/net/nicoulaj/idea/byteman/graphics/";
 
         /** The Byteman {@link javax.swing.Icon} (16x16px). */
         public static final Icon BYTEMAN_ICON_16 = IconLoader.getIcon(BYTEMAN_GRAPHICS_PATH + "byteman-logo-16.png");
 
         /** The Byteman {@link javax.swing.Icon} (24x24px). */
         public static final Icon BYTEMAN_ICON_24 = IconLoader.getIcon(BYTEMAN_GRAPHICS_PATH + "byteman-logo-24.png");
 
         /** The Byteman {@link javax.swing.Icon} (32x32px). */
         public static final Icon BYTEMAN_ICON_32 = IconLoader.getIcon(BYTEMAN_GRAPHICS_PATH + "byteman-logo-32.png");
 
         /** The Byteman {@link javax.swing.Icon} (64x64px). */
         public static final Icon BYTEMAN_ICON_64 = IconLoader.getIcon(BYTEMAN_GRAPHICS_PATH + "byteman-logo-64.png");
 
         /** The Byteman {@link javax.swing.Icon} (256x256px). */
         public static final Icon BYTEMAN_ICON_256 = IconLoader.getIcon(BYTEMAN_GRAPHICS_PATH + "byteman-logo-256.png");
 
         /** The Byteman file {@link javax.swing.Icon} (16x16px). */
         public static final Icon BYTEMAN_FILE_ICON_16 = IconLoader.getIcon(BYTEMAN_GRAPHICS_PATH + "byteman-file-16.png");
     }
 
     /** {@link java.util.ResourceBundle} utils for the Byteman plugin. */
     public static class Bundle {
 
         /** The {@link java.util.ResourceBundle} path. */
         @NonNls
         protected static final String BUNDLE_NAME = "net.nicoulaj.idea.byteman.bundle.strings";
 
         /**
          * The {@link java.util.ResourceBundle} instance.
          *
          * @see #BUNDLE_NAME
          */
         protected static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
 
         /** {@link net.nicoulaj.idea.byteman.BytemanResources.Bundle} is a non-instantiable static class. */
         private Bundle() {}
 
         /**
          * Load a {@link String} from the {@link #BUNDLE} {@link java.util.ResourceBundle}.
          *
          * @param key    the key of the resource.
          * @param params the optional parameters for the specific resource.
          * @return the {@link String} value or {@code null} if no resource found for the key.
          */
         public static String message(@PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
             return CommonBundle.message(BUNDLE, key, params);
         }
     }
 
     /** Samples for the Byteman language support plugin. */
     public static class Samples {
 
         /** {@link net.nicoulaj.idea.byteman.BytemanResources.Samples} is a non-instantiable static class. */
         private Samples() {}
 
         /** Path to Byteman graphic resources. */
         public static final String BYTEMAN_SAMPLES_PATH = "/net/nicoulaj/idea/byteman/samples/";
 
         /** The path to the sample Byteman document shown in the colors settings dialog. */
         @NonNls
         public static final String BYTEMAN_SAMPLE_TRACE_THREADS = BYTEMAN_SAMPLES_PATH + "trace-threads.btm";
     }
 }

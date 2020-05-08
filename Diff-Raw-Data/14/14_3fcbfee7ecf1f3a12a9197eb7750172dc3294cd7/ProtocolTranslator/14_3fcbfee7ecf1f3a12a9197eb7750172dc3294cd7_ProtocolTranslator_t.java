 /*
  * This file is part of CanaryModCoffeePotControlProtocol.
  *
  * Copyright © 2011-2014 Visual Illusions Entertainment
  *
  * CanaryModCoffeePotControlProtocol is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * CanaryModCoffeePotControlProtocol is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with CanaryModCoffeePotControlProtocol.
  * If not, see http://www.gnu.org/licenses/gpl.html.
  */
 /*
  * This file is part of dConomy.
  *
  * Copyright © 2011-2013 Visual Illusions Entertainment
  *
  * dConomy is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * dConomy is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with dConomy.
  * If not, see http://www.gnu.org/licenses/gpl.html.
  */
 package net.visualillusionsent.cmcpcp;
 
 import net.visualillusionsent.minecraft.plugin.ChatFormat;
 import net.visualillusionsent.minecraft.plugin.MessageTranslator;
 import net.visualillusionsent.minecraft.plugin.PluginInitializationException;
 import net.visualillusionsent.utils.FileUtils;
 import net.visualillusionsent.utils.JarUtils;
 import net.visualillusionsent.utils.LocaleHelper;
 
 import java.io.File;
 import java.io.FileInputStream;
 
 /** @author Jason (darkdiplomat) */
 public final class ProtocolTranslator extends MessageTranslator {
     private final String prefix = "$cA[$c6CMCPCP$cA] ";
 
     ProtocolTranslator(CanaryModCoffeePotControlProtocol cmcpcp, String locale) {
        super(cmcpcp, locale, cmcpcp.getController().updateLang());
     }
 
     public final String translate(String key, String locale, Object... args) {
         return ChatFormat.formatString(prefix.concat(localeTranslate(key, locale, args)), "$c");
     }
 }

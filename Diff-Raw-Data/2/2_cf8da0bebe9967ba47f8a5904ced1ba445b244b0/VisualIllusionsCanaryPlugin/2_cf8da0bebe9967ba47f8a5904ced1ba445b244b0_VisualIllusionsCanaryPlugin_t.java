 /*
  * This file is part of VIMCPlugin.
  *
  * Copyright © 2013-2014 Visual Illusions Entertainment
  *
  * VIMCPlugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * VIMCPlugin is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License along with VIMCPlugin.
  * If not, see http://www.gnu.org/licenses/lgpl.html.
  */
 package net.visualillusionsent.minecraft.plugin.canary;
 
 import net.canarymod.plugin.Plugin;
 import net.visualillusionsent.minecraft.plugin.VisualIllusionsMinecraftPlugin;
 import net.visualillusionsent.minecraft.plugin.VisualIllusionsPlugin;
 import net.visualillusionsent.utils.ProgramChecker;
 import net.visualillusionsent.utils.ProgramStatus;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Calendar;
 
 /**
  * Visual Illusions Canary Plugin extension
  *
  * @author Jason (darkdiplomat)
  */
 public abstract class VisualIllusionsCanaryPlugin extends Plugin implements VisualIllusionsPlugin {
 
     private final ProgramChecker pChecker;
     protected final boolean debug;
     protected final WrappedLogger logger;
 
     public VisualIllusionsCanaryPlugin() {
         this.debug = Boolean.valueOf(System.getProperty("debug.".concat(getName().toLowerCase()), "false"));
         this.pChecker = new ProgramChecker(getName(), getVersionArray(), getStatusURL(), getStatus());
         this.logger = new WrappedLogger(getLogman());
     }
 
     @Override
     public boolean enable() {
         VisualIllusionsMinecraftPlugin.checkVersion(this);
         VisualIllusionsMinecraftPlugin.checkStatus(this);
         return true;
     }
 
     @Override
     public final String getBuild() {
         return getCanaryInf().getString("build.number", "-1");
     }
 
     @Override
     public final String getBuildTime() {
         return getCanaryInf().getString("build.time", "19700101-0000");
     }
 
     @Override
     public final ProgramChecker getProgramChecker() {
         return pChecker;
     }
 
     @Override
     public final String getWikiURL() {
         return getCanaryInf().getString("wiki.url", "missing.url");
     }
 
     @Override
     public final String getIssuesURL() {
         return getCanaryInf().getString("issues.url", "missing.url");
     }
 
     @Override
     public final String getDevelopers() {
         return getCanaryInf().getString("developers", "missing.developers");
     }
 
     @Override
     public final String getCopyYear() {
         return getCanaryInf().getString("copyright.years", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
     }
 
     @Override
     public final ProgramStatus getStatus() {
         return getVersion().contains("-SNAPSHOT") ? ProgramStatus.SNAPSHOT : ProgramStatus.STABLE;
     }
 
     @Override
     public final long[] getVersionArray() {
         long[] mmr = new long[3];
        String[] vbreakdown = getVersion().replace("-SNAPSHOT", "").split("\\.");
         mmr[0] = Long.valueOf(vbreakdown[0]);
         mmr[1] = Long.valueOf(vbreakdown[1]);
         mmr[2] = Long.valueOf(vbreakdown[2]);
         return mmr;
     }
 
     private final URL getStatusURL() {
         try {
             return new URL(getCanaryInf().getString("status.url", "missing.url"));
         }
         catch (MalformedURLException e) {
             return null;
         }
     }
 
 }

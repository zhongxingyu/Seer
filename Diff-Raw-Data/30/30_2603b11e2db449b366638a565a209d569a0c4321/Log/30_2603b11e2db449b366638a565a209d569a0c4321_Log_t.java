 /**
  *  A Maven plugin to jury rig java class files with the Apache BCEL library.
  *  Copyright (C) 2012 NigelB
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2
  *  of the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  */
 package rigger;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
public abstract class Log{
      public static final Level ERROR = Level.SEVERE;
 
     public abstract void info(String message);
 
    public  abstract void error(String message);
//    {
//        log(ERROR, message);
//    }
 }

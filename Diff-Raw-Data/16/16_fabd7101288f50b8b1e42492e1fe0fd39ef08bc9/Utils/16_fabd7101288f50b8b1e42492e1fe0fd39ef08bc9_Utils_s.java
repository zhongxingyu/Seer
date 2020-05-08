 /*  copyit-server
  *  Copyright (C) 2013  Toon Schoenmakers
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.mms_projects.copy_it.api.oauth;
 
 import java.security.SecureRandom;
 
 public class Utils {
  private static final SecureRandom random = new SecureRandom();
  private static final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  public static String generateNonce() {
    char[] text = new char[8];
    for (int i = 0; i < 8; i++)
        text[i] = characters.charAt(random.nextInt(characters.length()));
    return new String(text);
  }
 }

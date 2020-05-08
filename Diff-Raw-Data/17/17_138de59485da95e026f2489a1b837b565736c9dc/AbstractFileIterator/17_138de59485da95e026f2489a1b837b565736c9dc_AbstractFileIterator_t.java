/*
 * File iteration library for plugins.
 * Copyright (C) 2013  Yuriy Kryshchuk
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses />.
 */
 package com.kryshchuk.maven.plugins.filevisitor;
 
 /**
  * Abstract AbstractFileIterator class.
  * 
  * @author yura
  * @since 1.0
  */
 public abstract class AbstractFileIterator {
 
   private final FileMapper fileMapper;
 
   protected AbstractFileIterator(final FileMapper fileMapper) {
     this.fileMapper = fileMapper;
   }
 
   public abstract void iterate(FileVisitor visitor) throws VisitorException;
 
   protected FileMapper getFileMapper() {
     return fileMapper;
   }
 
 }

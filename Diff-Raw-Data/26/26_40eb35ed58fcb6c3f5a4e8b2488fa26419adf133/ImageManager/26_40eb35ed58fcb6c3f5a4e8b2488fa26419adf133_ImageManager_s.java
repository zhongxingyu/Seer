 /*
  * Copyright (C) 2013 Geometer Plus <contact@geometerplus.com>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301, USA.
  */
 
 package org.geometerplus.fbreader.widget;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Environment;
 import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
 import org.geometerplus.fbreader.book.Book;
 import org.geometerplus.fbreader.book.IBookCollection;
 
 import java.io.File;
 
 public class ImageManager {
 	static final String PATH = Environment.getExternalStorageDirectory().toString() + "/FBReaderJ/Covers/";
 	static final String FORMAT = ".PNG";
 	static final int WIDTH = 470;
 	static final int HEIGHT = 650;
 
 	public static boolean saveCover(IBookCollection collection, Book book) {
 		return collection.saveCover(book, PATH + book.getId() + FORMAT);
 	}
 
 	public static boolean deleteAllCovers() {
 		final File file = new File(PATH);
 		if (file.isDirectory()) {
 			for (String child : file.list()) {
 				final File subFile = new File(PATH + child);
 				if (!subFile.delete()) {
 					return false;
 				}
 			}
 		}
 		return file.delete();
 	}
 
 	public static Bitmap getImage(Book book) {
 		final String imageInSD = PATH + book.getId() + FORMAT;
 		Bitmap cover = BitmapFactory.decodeFile(imageInSD);
 		if (cover == null) {
 			return null;
 		}
		cover = Bitmap.createScaledBitmap(cover, WIDTH, HEIGHT, false);
		return cover;
 	}
 }

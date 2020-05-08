 /*
  * Copyright (c) 2009-2012  Cologne Intelligence GmbH
  * This file is part of FitGoodies.
  *
  * FitGoodies is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * FitGoodies is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with FitGoodies.  If not, see <http://www.gnu.org/licenses/>.
  */
 
package ${groupId};
 
 import de.cologneintelligence.fitgoodies.ColumnFixture;
 
 public final class BookFixture extends ColumnFixture {
 	public String author;
 	public ISBN isbn;
 	public StringBuffer title;
 	public float price;
 	public Integer lookup;
 
 	public Integer save() {
 		return FixtureObjects.SHELF.addBook(
 				new Book(title, author, isbn, price));
 	}
 
 	public boolean valid() {
 		return new Book(title, author, isbn, price).isValid();
 	}
 
 	public boolean lookup() {
 		try {
 			Book book = FixtureObjects.SHELF.get(lookup);
 			author = book.getAuthor();
 			isbn = book.getIsbn();
 			title = book.getTitle();
 			price = book.getPrice();
 			return true;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	public String author() { return author; }
 	public StringBuffer title() { return title; }
 	public float price() { return price; }
 	public ISBN isbn() { return isbn; }
 }

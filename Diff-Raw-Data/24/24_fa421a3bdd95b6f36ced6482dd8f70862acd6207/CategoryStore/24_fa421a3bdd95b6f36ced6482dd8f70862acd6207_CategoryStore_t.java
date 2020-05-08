 package org.nuthatchery.pgf.tokens;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
import java.util.NoSuchElementException;
 import java.util.Set;
 
 import org.nuthatchery.pgf.tokens.errors.StoreNotCompiled;
 import org.nuthatchery.pgf.tokens.errors.StoreNotEditable;
 import org.nuthatchery.pgf.tokens.errors.UnknownCategory;
 
 public class CategoryStore {
	protected final Map<String, Category> cats = new HashMap<String, Category>();
	protected final Map<Category, Set<Category>> superMap = new IdentityHashMap<Category, Set<Category>>();
	protected int[] infoTable;
	protected Category[] catsByInt;
	protected int numCats;
	protected boolean editable = true;
 
 
 	/**
 	 * Find the category associated with a name.
 	 * 
 	 * @param name
 	 *            The name
 	 * @return The category
 	 * @throws UnknownCategory
 	 *             if the category has not been declare
 	 */
 	public Category category(String name) {
 		Category category = cats.get(name);
 		if(category == null) {
 			throw new UnknownCategory(name);
 		}
 		return category;
 
 	}
 
 
 	public boolean checkInvariant() {
 		if(!editable) {
			if(catsByInt == null || infoTable == null) {
 				return false;
 			}
 
 			if(catsByInt.length != numCats || infoTable.length != numCats * numCats) {
 				return false;
 			}
 
 			for(int x = 0; x < numCats; x++) {
 				Category xCat = catsByInt[x];
 				for(int y = 0; y < numCats; y++) {
 					Category yCat = catsByInt[y];
 
 					if(xCat == yCat) {
 						if(!(get(x, y) == 0)) {
 							System.err.println("Invariant error at x=" + x + ", y=" + y + ": expected 0, found " + get(x, y));
 							return false;
 						}
 						continue;
 					}
 
 					int val = get(x, y);
 					if(val < 0) {
 						if(val != -stepsToSuper(xCat, yCat)) {
 							System.err.println("Invariant error at x=" + x + ", y=" + y + ": expected " + val + ", found " + -stepsToSuper(xCat, yCat));
 							return false;
 						}
 					}
 					else if(val > 0) {
 						if(val != stepsToSuper(yCat, xCat)) {
 							System.err.println("Invariant error at x=" + x + ", y=" + y + ": expected " + val + ", found " + stepsToSuper(yCat, xCat));
 							return false;
 						}
 					}
 					else if(xCat == yCat) {
 						;
 					}
 					else if(stepsToSuper(xCat, yCat) != Integer.MAX_VALUE) {
 						System.err.println("Invariant error at x=" + x + ", y=" + y + ": expected MAX_VALUE, found " + stepsToSuper(xCat, yCat));
 						return false;
 					}
 					else if(stepsToSuper(yCat, xCat) != Integer.MAX_VALUE) {
 						System.err.println("Invariant error at x=" + x + ", y=" + y + ": expected MAX_VALUE, found " + stepsToSuper(yCat, xCat));
 						return false;
 					}
 				}
 			}
 		}
 		return true;
 
 	}
 
 
 	public void compile() {
 		editable = false;
 		infoTable = new int[numCats * numCats];
 		catsByInt = new Category[numCats];
 		for(Entry<String, Category> e : cats.entrySet()) {
 			catsByInt[e.getValue().getId()] = e.getValue();
 		}
 
 		for(int x = 0; x < numCats; x++) {
 			Category xCat = catsByInt[x];
 			for(int y = 0; y < x; y++) {
 				Category yCat = catsByInt[y];
 				int rel = 0;
 
 				int xToY = stepsToSuper(xCat, yCat);
 				int yToX = stepsToSuper(yCat, xCat);
 
 				if(xToY != Integer.MAX_VALUE) {
 					rel = -xToY;
 				}
 				else if(yToX != Integer.MAX_VALUE) {
 					rel = yToX;
 				}
 
 				put(x, y, rel);
 				put(y, x, -rel);
 
 			}
 		}
 
 		assert checkInvariant();
 	}
 
 
 	/**
 	 * Declare a category and zero or more associated supercategories.
 	 * 
 	 * The category are declared if not previously known, but the
 	 * supercategories must have been declared.
 	 * 
 	 * Further supercategories may be added by a subsequent call.
 	 * 
 	 * @param name
 	 *            The name of the category
 	 * @param supers
 	 *            The name of its supercategories
 	 * @return The category
 	 * @throws StoreNotEditable
 	 *             if the store is not open for changes
 	 * @throws UnknownCategory
 	 *             if a supercategory is unknown
 	 */
 	public Category declare(String name, String... supers) {
 		if(!editable) {
 			throw new StoreNotEditable();
 		}
 
 		Category category = cats.get(name);
 		if(category == null) {
 			category = new CategoryImpl(name, numCats++, this);
 			cats.put(name, category);
 		}
 
 		for(String s : supers) {
 			declareSuper(category, category(s));
 		}
 		return category;
 	}
 
 
 	public int numCats() {
 		return numCats;
 	}
 
 
 	public Iterable<Category> subCategoriesOf(final Category cat) {
 		return new Iterable<Category>() {
 
 			@Override
 			public Iterator<Category> iterator() {
 				return new Iterator<Category>() {
 					final int c1 = cat.getId();
 					int c2 = 0;
 
 
 					@Override
 					public boolean hasNext() {
 						while(c2 < numCats) {
 							if(get(c1, c2) > 0) {
 								return true;
 							}
 							c2++;
 						}
 						return false;
 					}
 
 
 					@Override
 					public Category next() {
						while(c2 < numCats && get(c1, c2) <= 0) {
							c2++;
						}
						if(c2 >= numCats) {
							throw new NoSuchElementException();
						}
 						return catsByInt[c2++];
 					}
 
 
 					@Override
 					public void remove() {
 						throw new UnsupportedOperationException();
 					}
 
 				};
 			}
 		};
 	}
 
 
 	public int superDist(Category cat1, Category cat2) {
 		if(editable) {
 			throw new StoreNotCompiled();
 		}
 
 		return get(cat1.getId(), cat2.getId());
 	}
 
 
 	/**
 	 * Produce an HTML representation of the store's super relation table.
 	 * 
 	 * @return An HTML string
 	 */
 	public String toHTML() {
 		if(editable) {
 			throw new StoreNotCompiled();
 		}
 
 		StringBuilder buf = new StringBuilder();
 		buf.append("<table>\n");
 		buf.append("  <tr><th></th>");
 		for(int x = 0; x < numCats; x++) {
 			buf.append("<th style=\"font-size: small\">");
 			buf.append(catsByInt[x].getName());
 			buf.append("</th>");
 		}
 		buf.append("</tr>\n");
 
 		for(int y = 0; y < numCats; y++) {
 			buf.append("  <tr><th style=\"font-size: small\">");
 			buf.append(catsByInt[y].getName());
 			buf.append("</th>");
 
 			for(int x = 0; x < numCats; x++) {
 				if(x == y) {
 					buf.append("<td>&ndash;</td>");
 				}
 				else {
 					buf.append("<td>");
 					buf.append(get(x, y));
 					buf.append("</td>");
 				}
 			}
 			buf.append("</tr>\n");
 		}
 		buf.append("</table>\n");
 
 		return buf.toString();
 	}
 
 
 	public String toJava(String storeName, boolean makeDecls, boolean makeStatic) {
 		StringBuilder buf = new StringBuilder();
 
 		for(Category c : catsByInt) {
 			if(makeDecls) {
 				if(makeStatic) {
 					buf.append("public static ");
 				}
 
 				buf.append("final Category ");
 			}
 			buf.append(c.getName());
 			buf.append(" = ");
 			buf.append(storeName);
 			buf.append(".category(\"");
 			buf.append(c.getName());
 			buf.append("\");\n");
 		}
 		return buf.toString();
 	}
 
 
 	private void declareSuper(Category subCat, Category superCat) {
 		if(subCat.getStore() != this || superCat.getStore() != this) {
 			throw new IllegalArgumentException("Category does not belong to this store");
 		}
 		Set<Category> supers = superMap.get(subCat);
 
 		if(supers == null) {
 			supers = new HashSet<Category>();
 			superMap.put(subCat, supers);
 		}
 
 		supers.add(superCat);
 	}
 
 
 	/**
 	 * 
 	 * get(X, Y) < 0 means category X is descendant of Y, level |get(X,Y)|
 	 * get(X, Y) > 0 means category X is ancestor of Y, level |get(X,Y)|
 	 * get(X, Y) == 0 means X and Y are not directly related
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	protected int get(int x, int y) {
 		if(x < 0 || x >= numCats || y < 0 || y >= numCats) {
 			throw new IllegalArgumentException();
 		}
 
 		return infoTable[y * numCats + x];
 	}
 
 
 	protected void put(int x, int y, int val) {
 		if(x < 0 || x >= numCats || y < 0 || y >= numCats) {
 			throw new IllegalArgumentException();
 		}
 
 		infoTable[y * numCats + x] = val;
 	}
 
 
 	/**
 	 * Return the miminum number of steps to reach cat2 from cat1 in the
 	 * super-chain
 	 * 
 	 * @param cat1
 	 * @param cat2
 	 * @return 0 if cat1 == cat2, >0 if cat1 <: cat2 or MAX_VALUE if cat2 is not
 	 *         an ancestor of cat1
 	 */
 	int stepsToSuper(Category cat1, Category cat2) {
 		if(cat1 == cat2) {
 			return 0;
 		}
 
 		int best = Integer.MAX_VALUE;
 
 		Set<Category> supers = superMap.get(cat1);
 		if(supers != null) {
 			for(Category s : superMap.get(cat1)) {
 				int toSuper = stepsToSuper(s, cat2);
 				if(toSuper != Integer.MAX_VALUE) {
 					best = Math.min(best, toSuper + 1);
 				}
 			}
 		}
 
 		return best;
 	}
 
 
 	Collection<Category> supersOf(Category cat) {
 		Set<Category> supers = superMap.get(cat);
 		if(supers == null) {
 			return Collections.EMPTY_SET;
 		}
 		else {
 			return Collections.unmodifiableCollection(supers);
 		}
 	}
 
 
 	static class CategoryImpl implements Category {
 		private final String name;
 		private final CategoryStore store;
 		private final int num;
 
 
 		public CategoryImpl(String name, int num, CategoryStore categoryStore) {
 			this.name = name;
 			this.store = categoryStore;
 			this.num = num;
 		}
 
 
 		@Override
 		public boolean equals(Object obj) {
 			return this == obj;
 		}
 
 
 		@Override
 		public int getId() {
 			return num;
 		}
 
 
 		@Override
 		public String getName() {
 			return name;
 		}
 
 
 		@Override
 		public CategoryStore getStore() {
 			return store;
 		}
 
 
 		@Override
 		public int hashCode() {
 			return System.identityHashCode(this);
 		}
 
 
 		@Override
 		public boolean isSubCatOf(Category category) {
 			if(this == category) {
 				return true;
 			}
 
 			if(store.editable) {
 				throw new StoreNotCompiled();
 			}
 
 			return store.get(num, category.getId()) < 0;
 		}
 
 
 		@Override
 		public String toString() {
 			return name;
 		}
 	}
 }

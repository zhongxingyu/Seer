 /*
  * Copyright (c) 2010, 2011, 2012 Thomas Kühne <thomas@kuehne.cn>
  * 
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
  * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
  * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package cn.kuehne.kinaseblender.gui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import cn.kuehne.kinaseblender.engine2.CompiledCloud;
 import cn.kuehne.kinaseblender.engine2.Product;
 import cn.kuehne.kinaseblender.engine2.Source;
 
 public class SourcesModel extends AbstractBasicTableModel {
 	private static final long serialVersionUID = 1L;
 	private final Object[][] data;
 
 	public SourcesModel(final CompiledCloud compiled) {
 		super(" Source");
 
 		final int sources = compiled.getSourceCount();
 		int maxProducts = 0;
 
 		final ArrayList<List<Product>> products = new ArrayList<List<Product>>();
 		for (int i = 0; i < sources; i++) {
 			final Source source = compiled.getSource(i);
 
 			final List<Product> produce = compiled.getProducts(source);
 			products.add(produce);
 			if (produce != null) {
 				final int size = produce.size();
 				if (size > maxProducts) {
 					maxProducts = size;
 				}
 			}
 		}
 
 		data = new Object[4 + maxProducts][sources + 1];
 
 		data[0][0] = "Number";
 		data[1][0] = "Source";
 		data[2][0] = "Amount";
 		data[3][0] = "Products";
 		for (int si = 0; si < maxProducts; si++) {
			data[si + 4][0] = "Product_" + (si+1);
 		}
 		for (int si = 0; si < sources; si++) {
 			data[0][si + 1] = (si + 1);
 			data[1][si + 1] = compiled.getSource(si);
 			final List<Product> produce = products.get(si);
 			if (produce == null) {
 				data[3][si + 1] = -1;
 			} else {
 				double amount = 0;
 				final int max = produce.size();
 				data[3][si + 1] = max;
 				for (int pi = 0; pi < max; pi++) {
 					data[pi + 4][si + 1] = produce.get(pi);
 					amount += compiled.getValue(si,
 							compiled.getProduct(produce.get(pi)));
 				}
 				data[2][si + 1] = amount;
 			}
 		}
 	}
 
 	@Override
 	protected Object[][] getData() {
 		return data;
 	}
 }

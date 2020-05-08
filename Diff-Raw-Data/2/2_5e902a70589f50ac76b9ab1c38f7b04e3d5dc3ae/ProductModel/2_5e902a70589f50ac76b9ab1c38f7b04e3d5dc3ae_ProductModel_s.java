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
 
 public class ProductModel extends AbstractBasicTableModel {
 	private static final long serialVersionUID = 1L;
 
 	private final Object[][] data;
 
 	public ProductModel(final CompiledCloud compiled) {
 		super(" Product");
 
 		final int products = compiled.getProductCount();
 		int maxSources = 0;
 
 		final List<List<Source>> sources = new ArrayList<List<Source>>();
 		for (int i = 0; i < products; i++) {
 			final Product product = compiled.getProduct(i);
 
 			final List<Source> producers = compiled.getSources(product);
 			sources.add(producers);
 			if (producers != null) {
 				final int size = producers.size();
 				if (size > maxSources) {
 					maxSources = size;
 				}
 			}
 		}
 
 		data = new Object[4 + maxSources][products + 1];
 
 		data[0][0] = "Number";
 		data[1][0] = "Product";
 		data[2][0] = "Amount";
 		data[3][0] = "Sources";
 		for (int si = 0; si < maxSources; si++) {
			data[si + 4][0] = "Source_" + si;
 		}
 		for (int pi = 0; pi < products; pi++) {
 			final Product product = compiled.getProduct(pi);
 			data[0][pi + 1] = (pi + 1);
 			data[1][pi + 1] = product;
 			final List<Source> producers = sources.get(pi);
 			if (producers == null) {
 				data[3][pi + 1] = -1;
 			} else {
 				double produce = 0;
 				final int max = producers.size();
 				data[3][pi + 1] = max;
 				for (int si = 0; si < max; si++) {
 					final Source source = producers.get(si);
 					data[si + 4][pi + 1] = source;
 					produce += compiled
 							.getValue(compiled.getSource(source), pi);
 				}
 				data[2][pi + 1] = produce;
 			}
 		}
 	}
 
 	@Override
 	protected Object[][] getData() {
 		return data; // NOPMD
 	}
 }

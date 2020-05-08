 /*
 * Copyright (c) 2012 Thomas Kuehne <thomas@kuehne.cn>
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
 package cn.kuehne.kinaseblender.engine2;
 
 import java.util.LinkedList;
 
 /**
  * Buffer used by SearchState
  */
 class TransferBuffer<T extends Scored> {
 
 	private final LinkedList<T> data;
 	private final int maxSize;
 	private int minScore;
 
 	TransferBuffer(final int maxElements) {
 		data = new LinkedList<T>();
 		maxSize = maxElements;
 	}
 
 	/**
 	 * add a new element if it has a good score and isn't yet contained herein 
 	 */
 	boolean add(final T comb) {
 		int score = comb.getScore();
 		if (score < minScore) {
 			return false;
 		}
 
 		int added = -1;
 		for (int index = 0; index < data.size(); index++) {
 			final T old = data.get(index);
 
 			if (comb.equals(old)) {
 				return false;
 			}
 
 			final int oldScore = old.getScore();
 
 			if (score < oldScore) {
 				if (score < oldScore && maxSize < data.size()) {
 					throw new IllegalStateException("old.score == score "
 							+ score + " " + data.size());
 
 				}
 				if (data.size() < maxSize || index != 0) {
 					data.add(index, comb);
 					added = index;
 					break;
 				} else {
 					return false;
 				}
 			}
 		}
 
 		if (added < 0) {
 			data.add(comb);
 		}
 		if (maxSize < data.size()) {
			T old = data.get(0);
 			if (data.getLast().getScore() == old.getScore()) {
 				throw new IllegalStateException("last.score == first.score "
 						+ score + " " + data.size() + " "
 						+ added);

			}
 			data.remove(0);
 		}
 
 		return true;
 	}
 
 	/**
 	 * allocate array and fill with current elements
 	 */
 	Scored[] copy() {
 		return data.toArray(new Scored[data.size()]);
 	}
 
 	/**
 	 * set a new minimal score
 	 */
 	void setMinScore(final int newMinScore) {
 		minScore = newMinScore;
 
 		data.clear();
 	}
 
 	/**
 	 * current number of elements
 	 */
 	int size() {
 		return data.size();
 	}
 }

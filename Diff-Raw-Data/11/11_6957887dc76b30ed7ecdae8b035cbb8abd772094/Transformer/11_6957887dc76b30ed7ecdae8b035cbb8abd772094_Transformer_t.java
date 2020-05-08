 /*
  * Copyright (c) 2009, Julian Gosnell
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *     * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above
  *     copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided
  *     with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.dada.core;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * A Connector that performs a one-for-one pluggable transformation on Updates flowing through it.
 
  * @author jules
  *
  * @param <IV>
  * @param <OV>
  */
 public class Transformer<IV, OV> extends Connector<IV, OV> {
 
 	public interface Transform<IV, OV> {
 		OV transform(IV value);
 	}
 	
 	private final Transform<IV, OV> transform;
 	
 	public Transformer(Collection<View<OV>> views, Transform<IV, OV> transform) {
 		super(views);
 		this.transform = transform;
 	}
 
 	protected Update<OV> transform(Update<IV> input) {
 		IV oldValue = input.getOldValue();
 		IV newValue = input.getNewValue();
 		return new Update<OV>(oldValue == null ? (OV)null : transform.transform(oldValue),
 							  newValue == null ? (OV)null : transform.transform(newValue));
 	}
 
 	protected Collection<Update<OV>> transform(Collection<Update<IV>> inputs) {
 		Collection<Update<OV>> outputs = new ArrayList<Update<OV>>(inputs.size());
 		for (Update<IV> input: inputs) {
 			outputs.add(transform(input));
 		}
 		return outputs;
 	}
 
 	@Override
 	public void update(Collection<Update<IV>> insertions, Collection<Update<IV>> updates, Collection<Update<IV>> deletions) {
 		for (View<OV> view : getViews()) {
 			view.update(transform(insertions), transform(updates), transform(deletions));
 		}
 	}
 
 }

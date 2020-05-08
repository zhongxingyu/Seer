 /*
  *  This file is part of Cotopaxi.
  *
  *  Cotopaxi is free software: you can redistribute it and/or modify
  *  it under the terms of the Lesser GNU General Public License as published
  *  by the Free Software Foundation, either version 3 of the License, or
  *  any later version.
  *
  *  Cotopaxi is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  Lesser GNU General Public License for more details.
  *
  *  You should have received a copy of the Lesser GNU General Public License
  *  along with Cotopaxi. If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.cotopaxi.validation.rule;
 
 
 
 /**
  * A rule to verify the maximum input length.
  * 
  * It's exclusive, it means that input length should be equals or lesser than configured length.
  * 
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
  * 
  */
 public class MaximumLengthRule extends AbstractRule {
 
 	private int maxLength;
 
 	public MaximumLengthRule(String message, int maxLength) {
 		super(message);
 		this.maxLength = maxLength;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see br.octahedron.cotopaxi.validation.ValidationRule#isValid(java.lang.String)
 	 */
 	@Override
 	public boolean isValid(String input) {
		return input.length() <= this.maxLength;
 	}
 }

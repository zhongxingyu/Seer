 package fi.vincit.jmobster.processor.languages.javascript.valueconverters;
 /*
  * Copyright 2012 Juha Siponen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 /**
  * Converter for Java Enums. Primitive enum types are
  * boxed to Enum objects and converted using this
 * converter.
  */
 public final class EnumConverter extends StringConverter {
    /**
     * How enums are converted
     */
     public static enum EnumMode {
        /**
         * Enums are converted to strings. E.g. ENUM1 will be converted to "ENUM1"
         */
         STRING,
        /**
         * Enums are converted to ordinals (starting from zero)
         */
         ORDINAL
     }
 
     private final EnumMode enumMode;
 
    /**
     * Constructs an enum converter.
     * @param enumMode Enum conversion mode
     */
     public EnumConverter(EnumMode enumMode) {
         this.enumMode = enumMode;
     }
 
     @Override
     protected String getValueAsString( Object value ) {
         switch( enumMode ) {
             case STRING: return super.getValueAsString(value);
             case ORDINAL: return getEnumOrdinal(value);
             default:
                 throw new RuntimeException("Invalid enum mode: " + enumMode);
         }
     }
 
    /**
     * Returns the enum ordinal (first being zero) as string
     * @param value Value to convert (must be instance of Enum)
     * @return Enum ordinal as string
     */
     private String getEnumOrdinal( Object value ) {
         Enum enumValue = (Enum)value;
         return "" + enumValue.ordinal();
     }
 }

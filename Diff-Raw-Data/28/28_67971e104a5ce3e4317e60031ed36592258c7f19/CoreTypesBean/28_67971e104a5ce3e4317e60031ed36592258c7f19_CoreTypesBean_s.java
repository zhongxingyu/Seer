 /**
  * Copyright (C) 2008 Ivan S. Dubrov
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *         http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.google.code.nanorm.test.beans;
 
 import java.sql.Time;
 import java.sql.Timestamp;
import java.util.Date;
 
 /**
  *
  * @author Ivan Dubrov
  * @version 1.0 31.05.2008
  */
 public class CoreTypesBean {
     
     private int id;
     
     private byte primByte;
     
     private Byte wrapByte;
     
     private short primShort;
     
     private Short wrapShort;
     
     private int primInt;
     
     private Integer wrapInt;
     
     private long primLong;
     
     private Long wrapLong;
     
     private boolean primBoolean;
     
     private Boolean wrapBoolean;
     
     private char primChar;
     
     private Character wrapChar;
     
     private float primFloat;
     
     private Float wrapFloat;
     
     private double primDouble;
     
     private Double wrapDouble;
     
     private String string;
     
     private Date sqlDate;
     
     private Time sqlTime;
     
     private Timestamp sqlTimestamp;
     
     private java.util.Date date;
     
     /** @return Returns the id. */
     public int getId() {
         return id;
     }
 
     /** @param id The id to set. */
     public void setId(int id) {
         this.id = id;
     }
 
     /** @return Returns the primByte. */
     public byte getPrimByte() {
         return primByte;
     }
 
     /** @param primByte The primByte to set. */
     public void setPrimByte(byte primByte) {
         this.primByte = primByte;
     }
 
     /** @return Returns the wrapByte. */
     public Byte getWrapByte() {
         return wrapByte;
     }
 
     /** @param wrapByte The wrapByte to set. */
     public void setWrapByte(Byte wrapByte) {
         this.wrapByte = wrapByte;
     }
 
     /** @return Returns the primShort. */
     public short getPrimShort() {
         return primShort;
     }
 
     /** @param primShort The primShort to set. */
     public void setPrimShort(short primShort) {
         this.primShort = primShort;
     }
 
     /** @return Returns the wrapShort. */
     public Short getWrapShort() {
         return wrapShort;
     }
 
     /** @param wrapShort The wrapShort to set. */
     public void setWrapShort(Short wrapShort) {
         this.wrapShort = wrapShort;
     }
 
     /** @return Returns the primInt. */
     public int getPrimInt() {
         return primInt;
     }
 
     /** @param primInt The primInt to set. */
     public void setPrimInt(int primInt) {
         this.primInt = primInt;
     }
 
     /** @return Returns the wrapInt. */
     public Integer getWrapInt() {
         return wrapInt;
     }
 
     /** @param wrapInt The wrapInt to set. */
     public void setWrapInt(Integer wrapInt) {
         this.wrapInt = wrapInt;
     }
 
     /** @return Returns the primLong. */
     public long getPrimLong() {
         return primLong;
     }
 
     /** @param primLong The primLong to set. */
     public void setPrimLong(long primLong) {
         this.primLong = primLong;
     }
 
     /** @return Returns the wrapLong. */
     public Long getWrapLong() {
         return wrapLong;
     }
 
     /** @param wrapLong The wrapLong to set. */
     public void setWrapLong(Long wrapLong) {
         this.wrapLong = wrapLong;
     }
 
     /** @return Returns the primBoolean. */
     public boolean isPrimBoolean() {
         return primBoolean;
     }
 
     /** @param primBoolean The primBoolean to set. */
     public void setPrimBoolean(boolean primBoolean) {
         this.primBoolean = primBoolean;
     }
 
     /** @return Returns the wrapBoolean. */
     public Boolean getWrapBoolean() {
         return wrapBoolean;
     }
 
     /** @param wrapBoolean The wrapBoolean to set. */
     public void setWrapBoolean(Boolean wrapBoolean) {
         this.wrapBoolean = wrapBoolean;
     }
 
     /** @return Returns the primChar. */
     public char getPrimChar() {
         return primChar;
     }
 
     /** @param primChar The primChar to set. */
     public void setPrimChar(char primChar) {
         this.primChar = primChar;
     }
 
     /** @return Returns the wrapChar. */
     public Character getWrapChar() {
         return wrapChar;
     }
 
     /** @param wrapChar The wrapChar to set. */
     public void setWrapChar(Character wrapChar) {
         this.wrapChar = wrapChar;
     }
 
     /** @return Returns the primFloat. */
     public float getPrimFloat() {
         return primFloat;
     }
 
     /** @param primFloat The primFloat to set. */
     public void setPrimFloat(float primFloat) {
         this.primFloat = primFloat;
     }
 
     /** @return Returns the wrapFloat. */
     public Float getWrapFloat() {
         return wrapFloat;
     }
 
     /** @param wrapFloat The wrapFloat to set. */
     public void setWrapFloat(Float wrapFloat) {
         this.wrapFloat = wrapFloat;
     }
 
     /** @return Returns the primDouble. */
     public double getPrimDouble() {
         return primDouble;
     }
 
     /** @param primDouble The primDouble to set. */
     public void setPrimDouble(double primDouble) {
         this.primDouble = primDouble;
     }
 
     /** @return Returns the wrapDouble. */
     public Double getWrapDouble() {
         return wrapDouble;
     }
 
     /** @param wrapDouble The wrapDouble to set. */
     public void setWrapDouble(Double wrapDouble) {
         this.wrapDouble = wrapDouble;
     }
 
     /** @return Returns the string. */
     public String getString() {
         return string;
     }
 
     /** @param string The string to set. */
     public void setString(String string) {
         this.string = string;
     }
 
 	/** @return the sqlDate */
 	public Date getSqlDate() {
 		return sqlDate;
 	}
 
 	/** @param sqlDate the sqlDate to set */
 	public void setSqlDate(Date sqlDate) {
 		this.sqlDate = sqlDate;
 	}
 
 	/** @return the sqlTime */
 	public Time getSqlTime() {
 		return sqlTime;
 	}
 
 	/** @param sqlTime the sqlTime to set */
 	public void setSqlTime(Time sqlTime) {
 		this.sqlTime = sqlTime;
 	}
 
 	/** @return the sqlTimestamp */
 	public Timestamp getSqlTimestamp() {
 		return sqlTimestamp;
 	}
 
 	/** @param sqlTimestamp the sqlTimestamp to set */
 	public void setSqlTimestamp(Timestamp sqlTimestamp) {
 		this.sqlTimestamp = sqlTimestamp;
 	}
 
 	/** @return the date */
 	public java.util.Date getDate() {
 		return date;
 	}
 
 	/** @param date the date to set */
 	public void setDate(java.util.Date date) {
 		this.date = date;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((date == null) ? 0 : date.hashCode());
 		result = prime * result + id;
 		result = prime * result + (primBoolean ? 1231 : 1237);
 		result = prime * result + primByte;
 		result = prime * result + primChar;
 		long temp;
 		temp = Double.doubleToLongBits(primDouble);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		result = prime * result + Float.floatToIntBits(primFloat);
 		result = prime * result + primInt;
 		result = prime * result + (int) (primLong ^ (primLong >>> 32));
 		result = prime * result + primShort;
 		result = prime * result + ((sqlDate == null) ? 0 : sqlDate.hashCode());
 		result = prime * result + ((sqlTime == null) ? 0 : sqlTime.hashCode());
 		result = prime * result + ((sqlTimestamp == null) ? 0 : sqlTimestamp.hashCode());
 		result = prime * result + ((string == null) ? 0 : string.hashCode());
 		result = prime * result + ((wrapBoolean == null) ? 0 : wrapBoolean.hashCode());
 		result = prime * result + ((wrapByte == null) ? 0 : wrapByte.hashCode());
 		result = prime * result + ((wrapChar == null) ? 0 : wrapChar.hashCode());
 		result = prime * result + ((wrapDouble == null) ? 0 : wrapDouble.hashCode());
 		result = prime * result + ((wrapFloat == null) ? 0 : wrapFloat.hashCode());
 		result = prime * result + ((wrapInt == null) ? 0 : wrapInt.hashCode());
 		result = prime * result + ((wrapLong == null) ? 0 : wrapLong.hashCode());
 		result = prime * result + ((wrapShort == null) ? 0 : wrapShort.hashCode());
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		CoreTypesBean other = (CoreTypesBean) obj;
 		if (date == null) {
 			if (other.date != null)
 				return false;
 		} else if (!date.equals(other.date))
 			return false;
 		if (id != other.id)
 			return false;
 		if (primBoolean != other.primBoolean)
 			return false;
 		if (primByte != other.primByte)
 			return false;
 		if (primChar != other.primChar)
 			return false;
 		if (Double.doubleToLongBits(primDouble) != Double.doubleToLongBits(other.primDouble))
 			return false;
 		if (Float.floatToIntBits(primFloat) != Float.floatToIntBits(other.primFloat))
 			return false;
 		if (primInt != other.primInt)
 			return false;
 		if (primLong != other.primLong)
 			return false;
 		if (primShort != other.primShort)
 			return false;
 		if (sqlDate == null) {
 			if (other.sqlDate != null)
 				return false;
 		} else if (!sqlDate.equals(other.sqlDate))
 			return false;
 		if (sqlTime == null) {
 			if (other.sqlTime != null)
 				return false;
 		} else if (!sqlTime.equals(other.sqlTime))
 			return false;
 		if (sqlTimestamp == null) {
 			if (other.sqlTimestamp != null)
 				return false;
 		} else if (!sqlTimestamp.equals(other.sqlTimestamp))
 			return false;
 		if (string == null) {
 			if (other.string != null)
 				return false;
 		} else if (!string.equals(other.string))
 			return false;
 		if (wrapBoolean == null) {
 			if (other.wrapBoolean != null)
 				return false;
 		} else if (!wrapBoolean.equals(other.wrapBoolean))
 			return false;
 		if (wrapByte == null) {
 			if (other.wrapByte != null)
 				return false;
 		} else if (!wrapByte.equals(other.wrapByte))
 			return false;
 		if (wrapChar == null) {
 			if (other.wrapChar != null)
 				return false;
 		} else if (!wrapChar.equals(other.wrapChar))
 			return false;
 		if (wrapDouble == null) {
 			if (other.wrapDouble != null)
 				return false;
 		} else if (!wrapDouble.equals(other.wrapDouble))
 			return false;
 		if (wrapFloat == null) {
 			if (other.wrapFloat != null)
 				return false;
 		} else if (!wrapFloat.equals(other.wrapFloat))
 			return false;
 		if (wrapInt == null) {
 			if (other.wrapInt != null)
 				return false;
 		} else if (!wrapInt.equals(other.wrapInt))
 			return false;
 		if (wrapLong == null) {
 			if (other.wrapLong != null)
 				return false;
 		} else if (!wrapLong.equals(other.wrapLong))
 			return false;
 		if (wrapShort == null) {
 			if (other.wrapShort != null)
 				return false;
 		} else if (!wrapShort.equals(other.wrapShort))
 			return false;
 		return true;
 	}
 }
